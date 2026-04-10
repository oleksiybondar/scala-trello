package io.github.oleksiybondar.api.infrastructure.db.auth

import cats.effect.IO
import io.github.oleksiybondar.api.domain.auth.{AuthSession, RefreshToken, SessionId}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.user.SlickUserRepo
import io.github.oleksiybondar.api.testkit.fixtures.SlickUserRepoFixtures.withCleanDatabase
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import munit.FunSuite

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext

class SlickAuthSessionRepoSpec extends FunSuite {

  given ExecutionContext = ExecutionContext.global

  test("create persists a session that can be looked up by refresh token") {
    withRepos { (userRepo, authSessionRepo, user) =>
      val now     = Instant.parse("2026-03-30T10:15:30Z")
      val session = AuthSession(
        id = SessionId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
        refreshToken = RefreshToken(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        userId = user.id,
        createdAt = now,
        revokedAt = None,
        expiresAt = now.plusSeconds(30L * 24L * 60L * 60L)
      )

      for {
        _      <- userRepo.create(user)
        _      <- authSessionRepo.create(session)
        result <- authSessionRepo.findActiveByRefreshToken(session.refreshToken, now)
      } yield assertEquals(result, Some(session))
    }
  }

  test("rotateRefreshToken replaces the refresh token without extending expiry") {
    withRepos { (userRepo, authSessionRepo, user) =>
      val now              = Instant.parse("2026-03-30T10:15:30Z")
      val session          = AuthSession(
        id = SessionId(UUID.fromString("33333333-3333-3333-3333-333333333333")),
        refreshToken = RefreshToken(UUID.fromString("44444444-4444-4444-4444-444444444444")),
        userId = user.id,
        createdAt = now,
        revokedAt = None,
        expiresAt = now.plusSeconds(30L * 24L * 60L * 60L)
      )
      val nextRefreshToken = RefreshToken(UUID.fromString("55555555-5555-5555-5555-555555555555"))

      for {
        _              <- userRepo.create(user)
        _              <- authSessionRepo.create(session)
        rotated        <- authSessionRepo.rotateRefreshToken(
                            session.id,
                            session.refreshToken,
                            nextRefreshToken,
                            now
                          )
        oldSession     <- authSessionRepo.findActiveByRefreshToken(session.refreshToken, now)
        rotatedSession <- authSessionRepo.findActiveByRefreshToken(nextRefreshToken, now)
      } yield {
        assert(rotated)
        assertEquals(oldSession, None)
        assertEquals(rotatedSession.map(_.expiresAt), Some(session.expiresAt))
      }
    }
  }

  test("revokeByRefreshToken revokes the session") {
    withRepos { (userRepo, authSessionRepo, user) =>
      val now     = Instant.parse("2026-03-30T10:15:30Z")
      val session = AuthSession(
        id = SessionId(UUID.fromString("66666666-6666-6666-6666-666666666666")),
        refreshToken = RefreshToken(UUID.fromString("77777777-7777-7777-7777-777777777777")),
        userId = user.id,
        createdAt = now,
        revokedAt = None,
        expiresAt = now.plusSeconds(30L * 24L * 60L * 60L)
      )

      for {
        _      <- userRepo.create(user)
        _      <- authSessionRepo.create(session)
        _      <- authSessionRepo.revokeByRefreshToken(session.refreshToken, now.plusSeconds(60))
        result <-
          authSessionRepo.findActiveByRefreshToken(session.refreshToken, now.plusSeconds(60))
      } yield assertEquals(result, None)
    }
  }

  test("findActiveByRefreshToken returns none for an expired session") {
    withRepos { (userRepo, authSessionRepo, user) =>
      val now     = Instant.parse("2026-03-30T10:15:30Z")
      val session = AuthSession(
        id = SessionId(UUID.fromString("88888888-8888-8888-8888-888888888888")),
        refreshToken = RefreshToken(UUID.fromString("99999999-9999-9999-9999-999999999999")),
        userId = user.id,
        createdAt = now.minusSeconds(120),
        revokedAt = None,
        expiresAt = now.minusSeconds(1)
      )

      for {
        _      <- userRepo.create(user)
        _      <- authSessionRepo.create(session)
        result <- authSessionRepo.findActiveByRefreshToken(session.refreshToken, now)
      } yield assertEquals(result, None)
    }
  }

  test("deleteExpiredOrRevoked removes expired and revoked sessions but keeps active ones") {
    withRepos { (userRepo, authSessionRepo, user) =>
      val now            = Instant.parse("2026-03-30T10:15:30Z")
      val activeSession  = AuthSession(
        id = SessionId(UUID.fromString("10000000-0000-0000-0000-000000000001")),
        refreshToken = RefreshToken(UUID.fromString("20000000-0000-0000-0000-000000000001")),
        userId = user.id,
        createdAt = now.minusSeconds(60),
        revokedAt = None,
        expiresAt = now.plusSeconds(3600)
      )
      val expiredSession = AuthSession(
        id = SessionId(UUID.fromString("10000000-0000-0000-0000-000000000002")),
        refreshToken = RefreshToken(UUID.fromString("20000000-0000-0000-0000-000000000002")),
        userId = user.id,
        createdAt = now.minusSeconds(3600),
        revokedAt = None,
        expiresAt = now.minusSeconds(1)
      )
      val revokedSession = AuthSession(
        id = SessionId(UUID.fromString("10000000-0000-0000-0000-000000000003")),
        refreshToken = RefreshToken(UUID.fromString("20000000-0000-0000-0000-000000000003")),
        userId = user.id,
        createdAt = now.minusSeconds(120),
        revokedAt = Some(now.minusSeconds(30)),
        expiresAt = now.plusSeconds(3600)
      )

      for {
        _             <- userRepo.create(user)
        _             <- authSessionRepo.create(activeSession)
        _             <- authSessionRepo.create(expiredSession)
        _             <- authSessionRepo.create(revokedSession)
        deleted       <- authSessionRepo.deleteExpiredOrRevoked(now)
        activeResult  <- authSessionRepo.findActiveByRefreshToken(activeSession.refreshToken, now)
        expiredResult <- authSessionRepo.findActiveByRefreshToken(expiredSession.refreshToken, now)
        revokedResult <- authSessionRepo.findActiveByRefreshToken(revokedSession.refreshToken, now)
      } yield {
        assertEquals(deleted, 2)
        assertEquals(activeResult, Some(activeSession))
        assertEquals(expiredResult, None)
        assertEquals(revokedResult, None)
      }
    }
  }

  private def withRepos(
      run: (
          SlickUserRepo[IO],
          AuthSessionRepoSlick[IO],
          io.github.oleksiybondar.api.domain.user.User
      ) => IO[Unit]
  ): Unit =
    withCleanDatabase { db =>
      val user = UserFixtures.user(
        id = UserId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
        username = Some(io.github.oleksiybondar.api.domain.user.Username("spec-user-session")),
        email =
          Some(io.github.oleksiybondar.api.domain.user.Email("spec-user+session@example.com")),
        firstName = io.github.oleksiybondar.api.domain.user.FirstName("Spec Session"),
        lastName = io.github.oleksiybondar.api.domain.user.LastName("Test User")
      )

      run(
        new SlickUserRepo[IO](db),
        new AuthSessionRepoSlick[IO](db),
        user
      )
    }
}
