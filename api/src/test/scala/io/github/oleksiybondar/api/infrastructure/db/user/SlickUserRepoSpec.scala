package io.github.oleksiybondar.api.infrastructure.db.user

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.config.ConfigLoader
import io.github.oleksiybondar.api.domain.user.*
import io.github.oleksiybondar.api.infrastructure.db.MigrationRunner
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import munit.FunSuite
import slick.jdbc.PostgresProfile.api.*

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class SlickUserRepoSpec extends FunSuite {

  given ExecutionContext = ExecutionContext.global

  private lazy val config =
    ConfigLoader.load().fold(throw _, identity)

  private lazy val db =
    Database.forURL(
      url = config.database.db.url,
      user = config.database.db.user,
      password = config.database.db.password,
      driver = config.database.db.driver
    )

  private lazy val repo =
    {
      MigrationRunner.migrate(config.database)
      new SlickUserRepo[IO](db)
    }

  test("create persists a user that can be loaded by id") {
    val user = testUser("11111111-1111-1111-1111-111111111111", "alice", "alice@example.com")

    withCleanRepo { repo =>
      repo.create(user).unsafeRunSync()

      val result = repo.findById(user.id).unsafeRunSync()

      assertEquals(result, Some(user))
    }
  }

  test("findByUsername and findByEmail return the matching user") {
    val user = testUser("22222222-2222-2222-2222-222222222222", "bob", "bob@example.com")

    withCleanRepo { repo =>
      repo.create(user).unsafeRunSync()

      val byUsername = repo.findByUsername(Username("bob")).unsafeRunSync()
      val byEmail = repo.findByEmail(Email("bob@example.com")).unsafeRunSync()

      assertEquals(byUsername, Some(user))
      assertEquals(byEmail, Some(user))
    }
  }

  test("list returns all persisted users") {
    val firstUser = testUser("33333333-3333-3333-3333-333333333333", "charlie", "charlie@example.com")
    val secondUser = testUser("44444444-4444-4444-4444-444444444444", "diana", "diana@example.com")

    withCleanRepo { repo =>
      repo.create(firstUser).unsafeRunSync()
      repo.create(secondUser).unsafeRunSync()

      val result = repo.list.unsafeRunSync()

      assertEquals(result.sortBy(_.id.value.toString), List(firstUser, secondUser).sortBy(_.id.value.toString))
    }
  }

  test("update returns true and persists changes for an existing user") {
    val existingUser = testUser("55555555-5555-5555-5555-555555555555", "eve", "eve@example.com")
    val updatedUser =
      existingUser.copy(
        firstName = FirstName("Eva"),
        lastName = LastName("Updated"),
        avatarUrl = Some(AvatarUrl("https://example.com/eva.png"))
      )

    withCleanRepo { repo =>
      repo.create(existingUser).unsafeRunSync()

      val updated = repo.update(updatedUser).unsafeRunSync()
      val reloaded = repo.findById(existingUser.id).unsafeRunSync()

      assertEquals(updated, true)
      assertEquals(reloaded, Some(updatedUser))
    }
  }

  test("update returns false when the user does not exist") {
    val missingUser = testUser("66666666-6666-6666-6666-666666666666", "frank", "frank@example.com")

    withCleanRepo { repo =>
      val updated = repo.update(missingUser).unsafeRunSync()

      assertEquals(updated, false)
    }
  }

  test("delete returns true for an existing user and removes it") {
    val user = testUser("77777777-7777-7777-7777-777777777777", "grace", "grace@example.com")

    withCleanRepo { repo =>
      repo.create(user).unsafeRunSync()

      val deleted = repo.delete(user.id).unsafeRunSync()
      val reloaded = repo.findById(user.id).unsafeRunSync()

      assertEquals(deleted, true)
      assertEquals(reloaded, None)
    }
  }

  test("delete returns false when the user does not exist") {
    withCleanRepo { repo =>
      val deleted =
        repo.delete(UserId(UUID.fromString("88888888-8888-8888-8888-888888888888"))).unsafeRunSync()

      assertEquals(deleted, false)
    }
  }

  private def testUser(id: String, username: String, email: String): User =
    UserFixtures.user(
      id = UserId(UUID.fromString(id)),
      username = Some(Username(username)),
      email = Some(Email(email))
    )

  private def withCleanRepo[A](run: SlickUserRepo[IO] => A): A = {
    truncateUsersTable()
    run(repo)
  }

  private def truncateUsersTable(): Unit =
    Await.result(db.run(sqlu"TRUNCATE TABLE users"), 10.seconds)
}
