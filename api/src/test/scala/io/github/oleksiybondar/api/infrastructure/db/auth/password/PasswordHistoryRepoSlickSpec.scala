package io.github.oleksiybondar.api.infrastructure.db.auth.password

import cats.effect.IO
import io.github.oleksiybondar.api.domain.auth.password.{PasswordHistoryEntry, PasswordHistoryId}
import io.github.oleksiybondar.api.domain.user.{Email, PasswordHash, UserId, Username}
import io.github.oleksiybondar.api.infrastructure.db.user.SlickUserRepo
import io.github.oleksiybondar.api.testkit.fixtures.SlickUserRepoFixtures.withCleanDatabase
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import munit.FunSuite

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext

class PasswordHistoryRepoSlickSpec extends FunSuite {

  given ExecutionContext = ExecutionContext.global

  test("create persists a password history entry that can be loaded by user id") {
    withRepos { (userRepo, passwordHistoryRepo, user) =>
      val entry = PasswordHistoryEntry(
        id = PasswordHistoryId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
        userId = user.id,
        passwordHash = PasswordHash("hash-1"),
        createdAt = Instant.parse("2026-03-31T09:00:00Z")
      )

      for {
        _      <- userRepo.create(user)
        _      <- passwordHistoryRepo.create(entry)
        result <- passwordHistoryRepo.findByUserId(user.id)
      } yield assertEquals(result, List(entry))
    }
  }

  test("findByUserId returns entries ordered from newest to oldest") {
    withRepos { (userRepo, passwordHistoryRepo, user) =>
      val olderEntry = PasswordHistoryEntry(
        id = PasswordHistoryId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        userId = user.id,
        passwordHash = PasswordHash("hash-older"),
        createdAt = Instant.parse("2026-03-31T09:00:00Z")
      )
      val newerEntry = PasswordHistoryEntry(
        id = PasswordHistoryId(UUID.fromString("33333333-3333-3333-3333-333333333333")),
        userId = user.id,
        passwordHash = PasswordHash("hash-newer"),
        createdAt = Instant.parse("2026-03-31T10:00:00Z")
      )

      for {
        _      <- userRepo.create(user)
        _      <- passwordHistoryRepo.create(olderEntry)
        _      <- passwordHistoryRepo.create(newerEntry)
        result <- passwordHistoryRepo.findByUserId(user.id)
      } yield assertEquals(result, List(newerEntry, olderEntry))
    }
  }

  test("deleteByUserId removes all password history entries for the user") {
    withRepos { (userRepo, passwordHistoryRepo, user) =>
      val entry = PasswordHistoryEntry(
        id = PasswordHistoryId(UUID.fromString("44444444-4444-4444-4444-444444444444")),
        userId = user.id,
        passwordHash = PasswordHash("hash-1"),
        createdAt = Instant.parse("2026-03-31T09:00:00Z")
      )

      for {
        _      <- userRepo.create(user)
        _      <- passwordHistoryRepo.create(entry)
        _      <- passwordHistoryRepo.deleteByUserId(user.id)
        result <- passwordHistoryRepo.findByUserId(user.id)
      } yield assertEquals(result, Nil)
    }
  }

  test("deleteByIds removes only the selected history entries") {
    withRepos { (userRepo, passwordHistoryRepo, user) =>
      val firstEntry  = PasswordHistoryEntry(
        id = PasswordHistoryId(UUID.fromString("55555555-5555-5555-5555-555555555555")),
        userId = user.id,
        passwordHash = PasswordHash("hash-1"),
        createdAt = Instant.parse("2026-03-31T09:00:00Z")
      )
      val secondEntry = PasswordHistoryEntry(
        id = PasswordHistoryId(UUID.fromString("66666666-6666-6666-6666-666666666666")),
        userId = user.id,
        passwordHash = PasswordHash("hash-2"),
        createdAt = Instant.parse("2026-03-31T10:00:00Z")
      )

      for {
        _      <- userRepo.create(user)
        _      <- passwordHistoryRepo.create(firstEntry)
        _      <- passwordHistoryRepo.create(secondEntry)
        _      <- passwordHistoryRepo.deleteByIds(List(firstEntry.id))
        result <- passwordHistoryRepo.findByUserId(user.id)
      } yield assertEquals(result, List(secondEntry))
    }
  }

  private def withRepos(
      run: (
          SlickUserRepo[IO],
          PasswordHistoryRepoSlick[IO],
          io.github.oleksiybondar.api.domain.user.User
      ) => IO[Unit]
  ): Unit =
    withCleanDatabase { db =>
      val user = UserFixtures.user(
        id = UserId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
        username = Some(Username("spec-user-password-history")),
        email = Some(Email("spec-user+password-history@example.com")),
        firstName = io.github.oleksiybondar.api.domain.user.FirstName("Spec Password"),
        lastName = io.github.oleksiybondar.api.domain.user.LastName("Test User")
      )

      run(
        new SlickUserRepo[IO](db),
        new PasswordHistoryRepoSlick[IO](db),
        user
      )
    }
}
