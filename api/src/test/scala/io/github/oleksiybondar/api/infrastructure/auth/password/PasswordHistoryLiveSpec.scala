package io.github.oleksiybondar.api.infrastructure.auth.password

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref}
import io.github.oleksiybondar.api.config.{PasswordConfig, PasswordStrengthConfig}
import io.github.oleksiybondar.api.domain.auth.password.{
  PasswordHasher,
  PasswordHistoryEntry,
  PasswordHistoryId
}
import io.github.oleksiybondar.api.domain.user.{PasswordHash, UserId}
import io.github.oleksiybondar.api.infrastructure.db.auth.password.PasswordHistoryRepo
import munit.FunSuite

import java.util.UUID

class PasswordHistoryLiveSpec extends FunSuite {

  private val passwordConfig =
    PasswordConfig(
      pepper = "test-password-pepper",
      historySize = 5,
      strength = PasswordStrengthConfig(
        minLength = 8,
        requireDigit = false,
        requireSpecialChar = false
      )
    )

  test("record stores a password history entry") {
    val userId = UserId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))

    val result =
      withHistory(passwordConfig) { (history, repo) =>
        for {
          _       <- history.record(userId, PasswordHash("hash-1"))
          entries <- repo.findByUserId(userId)
        } yield entries
      }

    assertEquals(result.map(_.passwordHash), List(PasswordHash("hash-1")))
  }

  test("record prunes old history entries beyond the configured limit") {
    val userId                          = UserId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
    val passwordConfigWithHistoryLimit2 = passwordConfig.copy(historySize = 2)

    val result =
      withHistory(passwordConfigWithHistoryLimit2) { (history, repo) =>
        for {
          _       <- history.record(userId, PasswordHash("hash-1"))
          _       <- history.record(userId, PasswordHash("hash-2"))
          _       <- history.record(userId, PasswordHash("hash-3"))
          entries <- repo.findByUserId(userId)
        } yield entries
      }

    assertEquals(result.map(_.passwordHash), List(PasswordHash("hash-3"), PasswordHash("hash-2")))
  }

  test("wasUsedBefore returns true when the password matches a historical hash") {
    val userId = UserId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"))

    val result =
      withHistory(passwordConfig) { (history, _) =>
        for {
          _       <- history.record(userId, PasswordHash("hash:old-password"))
          wasUsed <- history.wasUsedBefore(userId, "old-password")
        } yield wasUsed
      }

    assertEquals(result, true)
  }

  test("wasUsedBefore returns false when the password was not used before") {
    val userId = UserId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"))

    val result =
      withHistory(passwordConfig) { (history, _) =>
        for {
          _       <- history.record(userId, PasswordHash("hash:old-password"))
          wasUsed <- history.wasUsedBefore(userId, "new-password")
        } yield wasUsed
      }

    assertEquals(result, false)
  }

  test("clear removes all history entries for the user") {
    val userId = UserId(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"))

    val result =
      withHistory(passwordConfig) { (history, repo) =>
        for {
          _       <- history.record(userId, PasswordHash("hash-1"))
          _       <- history.clear(userId)
          entries <- repo.findByUserId(userId)
        } yield entries
      }

    assertEquals(result, Nil)
  }

  private def withHistory[A](
      config: PasswordConfig
  )(run: (PasswordHistoryLive[IO], InMemoryPasswordHistoryRepo) => IO[A]): A =
    (for {
      repo   <- InMemoryPasswordHistoryRepo.create
      history = new PasswordHistoryLive[IO](repo, FakePasswordHasher, config)
      result <- run(history, repo)
    } yield result).unsafeRunSync()

  private object FakePasswordHasher extends PasswordHasher[IO] {
    override def hash(password: String): IO[PasswordHash] =
      IO.pure(PasswordHash(s"hash:$password"))

    override def verify(password: String, hash: PasswordHash): IO[Boolean] =
      IO.pure(hash == PasswordHash(s"hash:$password"))
  }

  private final class InMemoryPasswordHistoryRepo private (
      state: Ref[IO, Vector[PasswordHistoryEntry]]
  ) extends PasswordHistoryRepo[IO] {

    override def create(entry: PasswordHistoryEntry): IO[Unit] =
      state.update(entries => entry +: entries)

    override def findByUserId(userId: UserId): IO[List[PasswordHistoryEntry]] =
      state.get.map(
        _.filter(_.userId == userId)
          .toList
      )

    override def deleteByUserId(userId: UserId): IO[Unit] =
      state.update(_.filterNot(_.userId == userId))

    override def deleteByIds(ids: List[PasswordHistoryId]): IO[Unit] =
      state.update(entries => entries.filterNot(entry => ids.contains(entry.id)))
  }

  private object InMemoryPasswordHistoryRepo {
    def create: IO[InMemoryPasswordHistoryRepo] =
      Ref
        .of[IO, Vector[PasswordHistoryEntry]](Vector.empty)
        .map(new InMemoryPasswordHistoryRepo(_))
  }
}
