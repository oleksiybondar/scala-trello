package io.github.oleksiybondar.api.infrastructure.db.user

import io.github.oleksiybondar.api.domain.user._
import io.github.oleksiybondar.api.testkit.fixtures.SlickUserRepoFixtures.withCleanRepo
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import munit.FunSuite

import java.util.UUID

class SlickUserRepoSpec extends FunSuite {

  test("create persists a user that can be loaded by id") {
    val user =
      testUser(
        "11111111-1111-1111-1111-111111111111",
        "spec-user-alice",
        "spec-user+alice@example.com"
      )

    withCleanRepo { repo =>
      for {
        _      <- repo.create(user)
        result <- repo.findById(user.id)
      } yield assertEquals(result, Some(user))
    }
  }

  test("findByUsername and findByEmail return the matching user") {
    val user =
      testUser(
        "22222222-2222-2222-2222-222222222222",
        "spec-user-bob",
        "spec-user+bob@example.com"
      )

    withCleanRepo { repo =>
      for {
        _          <- repo.create(user)
        byUsername <- repo.findByUsername(Username("spec-user-bob"))
        byEmail    <- repo.findByEmail(Email("spec-user+bob@example.com"))
      } yield {
        assertEquals(byUsername, Some(user))
        assertEquals(byEmail, Some(user))
      }
    }
  }

  test("list returns all persisted users") {
    val firstUser  =
      testUser(
        "33333333-3333-3333-3333-333333333333",
        "spec-user-charlie",
        "spec-user+charlie@example.com"
      )
    val secondUser =
      testUser(
        "44444444-4444-4444-4444-444444444444",
        "spec-user-diana",
        "spec-user+diana@example.com"
      )

    withCleanRepo { repo =>
      for {
        _      <- repo.create(firstUser)
        _      <- repo.create(secondUser)
        result <- repo.list
      } yield assertEquals(
        result.sortBy(_.id.value.toString),
        List(firstUser, secondUser).sortBy(_.id.value.toString)
      )
    }
  }

  test("update returns true and persists changes for an existing user") {
    val existingUser =
      testUser(
        "55555555-5555-5555-5555-555555555555",
        "spec-user-eve",
        "spec-user+eve@example.com"
      )
    val updatedUser  =
      existingUser.copy(
        firstName = FirstName("Eva"),
        lastName = LastName("Updated"),
        avatarUrl = Some(AvatarUrl("https://example.com/eva.png"))
      )

    withCleanRepo { repo =>
      for {
        _        <- repo.create(existingUser)
        updated  <- repo.update(updatedUser)
        reloaded <- repo.findById(existingUser.id)
      } yield {
        assertEquals(updated, true)
        assertEquals(reloaded, Some(updatedUser))
      }
    }
  }

  test("update returns false when the user does not exist") {
    val missingUser =
      testUser(
        "66666666-6666-6666-6666-666666666666",
        "spec-user-frank",
        "spec-user+frank@example.com"
      )

    withCleanRepo { repo =>
      repo.update(missingUser).map(updated => assertEquals(updated, false))
    }
  }

  test("delete returns true for an existing user and removes it") {
    val user =
      testUser(
        "77777777-7777-7777-7777-777777777777",
        "spec-user-grace",
        "spec-user+grace@example.com"
      )

    withCleanRepo { repo =>
      for {
        _        <- repo.create(user)
        deleted  <- repo.delete(user.id)
        reloaded <- repo.findById(user.id)
      } yield {
        assertEquals(deleted, true)
        assertEquals(reloaded, None)
      }
    }
  }

  test("delete returns false when the user does not exist") {
    withCleanRepo { repo =>
      repo
        .delete(UserId(UUID.fromString("88888888-8888-8888-8888-888888888888")))
        .map(deleted => assertEquals(deleted, false))
    }
  }

  private def testUser(id: String, username: String, email: String): User =
    UserFixtures.user(
      id = UserId(UUID.fromString(id)),
      username = Some(Username(username)),
      email = Some(Email(email))
    )
}
