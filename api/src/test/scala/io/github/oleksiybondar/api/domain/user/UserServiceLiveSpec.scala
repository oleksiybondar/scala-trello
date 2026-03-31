package io.github.oleksiybondar.api.domain.user

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.auth.password.PasswordHistory
import io.github.oleksiybondar.api.domain.auth.password.PasswordStrengthError.PasswordTooShort
import io.github.oleksiybondar.api.testkit.fixtures.AuthServiceFixtures.{
  fakePasswordHasher,
  passwordStrengthValidator
}
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import io.github.oleksiybondar.api.testkit.fixtures.UserServiceFixtures.withUserService
import io.github.oleksiybondar.api.testkit.support.InMemoryUserRepo
import munit.FunSuite

import java.time.Instant
import java.util.UUID

class UserServiceLiveSpec extends FunSuite {

  test("createUser persists a user") {
    val createdUser =
      UserFixtures.user(
        id = UserId(UUID.fromString("99999999-9999-9999-9999-999999999999")),
        username = Some(Username("new-user")),
        email = Some(Email("new-user@example.com"))
      )

    withUserService() { ctx =>
      for {
        _        <- ctx.userService.createUser(createdUser)
        reloaded <- ctx.userService.getUser(createdUser.id)
      } yield reloaded
    } match {
      case result =>
        assertEquals(result, Some(createdUser))
    }
  }

  test("getUser returns the matching user") {
    val user = UserFixtures.sampleUser

    val result = withUserService(List(user)) { ctx =>
      ctx.userService.getUser(user.id)
    }

    assertEquals(result, Some(user))
  }

  test("getByUsername returns the matching user") {
    val user = UserFixtures.sampleUser

    val result = withUserService(List(user)) { ctx =>
      ctx.userService.getByUsername(Username("alice"))
    }

    assertEquals(result, Some(user))
  }

  test("getByEmail returns the matching user") {
    val user = UserFixtures.sampleUser

    val result = withUserService(List(user)) { ctx =>
      ctx.userService.getByEmail(Email("alice@example.com"))
    }

    assertEquals(result, Some(user))
  }

  test("listUsers returns all users") {
    val firstUser  = UserFixtures.sampleUser
    val secondUser =
      UserFixtures.user(
        id = UserId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
        username = Some(Username("bob")),
        email = Some(Email("bob@example.com"))
      )

    val result = withUserService(List(firstUser, secondUser)) { ctx =>
      ctx.userService.listUsers
    }

    assertEquals(
      result.sortBy(_.id.value.toString),
      List(firstUser, secondUser).sortBy(_.id.value.toString)
    )
  }

  test("listUsersPage returns the requested page of users") {
    val firstUser  =
      UserFixtures.user(
        id = UserId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
        username = Some(Username("alice")),
        email = Some(Email("alice@example.com")),
        createdAt = Instant.parse("2026-03-31T08:00:00Z")
      )
    val secondUser =
      UserFixtures.user(
        id = UserId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")),
        username = Some(Username("bob")),
        email = Some(Email("bob@example.com")),
        createdAt = Instant.parse("2026-03-31T09:00:00Z")
      )
    val thirdUser  =
      UserFixtures.user(
        id = UserId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")),
        username = Some(Username("charlie")),
        email = Some(Email("charlie@example.com")),
        createdAt = Instant.parse("2026-03-31T10:00:00Z")
      )

    val result = withUserService(List(firstUser, secondUser, thirdUser)) { ctx =>
      ctx.userService.listUsersPage(1, 1)
    }

    assertEquals(result, List(secondUser))
  }

  test("updateUser returns true and persists changes for an existing user") {
    val existingUser = UserFixtures.sampleUser
    val updatedUser  =
      existingUser.copy(
        firstName = FirstName("Alicia"),
        lastName = LastName("Updated")
      )

    val result = withUserService(List(existingUser)) { ctx =>
      for {
        updated  <- ctx.userService.updateUser(updatedUser)
        reloaded <- ctx.userService.getUser(existingUser.id)
      } yield (updated, reloaded)
    }

    val (updated, reloaded) = result
    assertEquals(updated, true)
    assertEquals(reloaded, Some(updatedUser))
  }

  test("updateUser returns false when the user does not exist") {
    val missingUser = UserFixtures.sampleUser

    val result = withUserService() { ctx =>
      ctx.userService.updateUser(missingUser)
    }

    assertEquals(result, false)
  }

  test("deleteUser returns true and removes the user when it exists") {
    val user = UserFixtures.sampleUser

    val result = withUserService(List(user)) { ctx =>
      for {
        deleted  <- ctx.userService.deleteUser(user.id)
        reloaded <- ctx.userService.getUser(user.id)
      } yield (deleted, reloaded)
    }

    val (deleted, reloaded) = result
    assertEquals(deleted, true)
    assertEquals(reloaded, None)
  }

  test("deleteUser returns false when the user does not exist") {
    val missingUserId = UserId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))

    val result = withUserService() { ctx =>
      ctx.userService.deleteUser(missingUserId)
    }

    assertEquals(result, false)
  }

  test("changeUsername returns UsernameRequired for a blank username") {
    val result = withUserService(List(UserFixtures.sampleUser)) { ctx =>
      ctx.userService.changeUsername(UserFixtures.sampleUser.id, "   ").value
    }

    assertEquals(result, Left(UserMutationError.UsernameRequired))
  }

  test("changeUsername returns UsernameAlreadyUsed when the username is taken by another user") {
    val otherUser =
      UserFixtures.user(
        id = UserId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
        username = Some(Username("taken-name")),
        email = Some(Email("other@example.com"))
      )

    val result = withUserService(List(UserFixtures.sampleUser, otherUser)) { ctx =>
      ctx.userService.changeUsername(UserFixtures.sampleUser.id, "taken-name").value
    }

    assertEquals(result, Left(UserMutationError.UsernameAlreadyUsed))
  }

  test("changeEmail returns EmailRequired for a blank email") {
    val result = withUserService(List(UserFixtures.sampleUser)) { ctx =>
      ctx.userService.changeEmail(UserFixtures.sampleUser.id, "   ").value
    }

    assertEquals(result, Left(UserMutationError.EmailRequired))
  }

  test("changeEmail returns InvalidEmail for a malformed email") {
    val result = withUserService(List(UserFixtures.sampleUser)) { ctx =>
      ctx.userService.changeEmail(UserFixtures.sampleUser.id, "not-an-email").value
    }

    assertEquals(result, Left(UserMutationError.InvalidEmail))
  }

  test("changeEmail returns EmailAlreadyUsed when the email is taken by another user") {
    val otherUser =
      UserFixtures.user(
        id = UserId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
        username = Some(Username("bob")),
        email = Some(Email("taken@example.com"))
      )

    val result = withUserService(List(UserFixtures.sampleUser, otherUser)) { ctx =>
      ctx.userService.changeEmail(UserFixtures.sampleUser.id, "taken@example.com").value
    }

    assertEquals(result, Left(UserMutationError.EmailAlreadyUsed))
  }

  test("changePassword returns InvalidCurrentPassword when the current password is wrong") {
    val result = withUserService(List(UserFixtures.sampleUser)) { ctx =>
      ctx.userService.changePassword(
        UserFixtures.sampleUser.id,
        "wrong-password",
        "secret123"
      ).value
    }

    assertEquals(result, Left(UserMutationError.InvalidCurrentPassword))
  }

  test("changePassword returns WeakPassword when the new password violates the strength policy") {
    val result = withUserService(List(UserFixtures.sampleUser)) { ctx =>
      ctx.userService.changePassword(UserFixtures.sampleUser.id, "secret", "short").value
    }

    result match {
      case Left(UserMutationError.WeakPassword(errors)) =>
        assertEquals(errors, List(PasswordTooShort(8)))
      case other                                        => fail(s"Expected WeakPassword, got: $other")
    }
  }

  test(
    "changePassword returns PasswordAlreadyUsed when the new password matches a previous password"
  ) {
    val result = (
      for {
        userRepo   <- InMemoryUserRepo.create[IO](List(UserFixtures.sampleUser))
        userService = new UserServiceLive[IO](
                        userRepo,
                        fakePasswordHasher,
                        passwordStrengthValidator,
                        new PasswordHistory[IO] {
                          override def record(userId: UserId, hash: PasswordHash): IO[Unit] =
                            IO.unit

                          override def wasUsedBefore(
                              userId: UserId,
                              password: String
                          ): IO[Boolean] =
                            IO.pure(password == "secret123")

                          override def clear(userId: UserId): IO[Unit] =
                            IO.unit
                        }
                      )
        result     <-
          userService.changePassword(UserFixtures.sampleUser.id, "secret", "secret123").value
      } yield result
    ).unsafeRunSync()

    assertEquals(result, Left(UserMutationError.PasswordAlreadyUsed))
  }

  test("changePassword returns UserNotFound when the user does not exist") {
    val missingUserId = UserId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"))

    val result = withUserService() { ctx =>
      ctx.userService.changePassword(missingUserId, "secret", "secret123").value
    }

    assertEquals(result, Left(UserMutationError.UserNotFound))
  }
}
