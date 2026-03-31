package io.github.oleksiybondar.api.domain.user

import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import io.github.oleksiybondar.api.testkit.fixtures.UserServiceFixtures.withUserService
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
}
