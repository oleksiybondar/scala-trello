package io.github.oleksiybondar.api.testkit.support

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.auth.{AuthSession, RefreshToken, SessionId}
import io.github.oleksiybondar.api.domain.user.UserId
import munit.FunSuite

import java.time.Instant
import java.util.UUID

class InMemoryAuthRepoSpec extends FunSuite {

  test("deleteExpiredOrRevoked removes revoked and expired sessions only") {
    val now = Instant.parse("2026-03-30T10:15:30Z")

    val result = for {
      repo    <- InMemoryAuthRepo.create[IO]()
      _       <- repo.create(session(
                   "10000000-0000-0000-0000-000000000011",
                   "20000000-0000-0000-0000-000000000011",
                   expiresAt = now.plusSeconds(3600),
                   revokedAt = None
                 ))
      _       <- repo.create(session(
                   "10000000-0000-0000-0000-000000000012",
                   "20000000-0000-0000-0000-000000000012",
                   expiresAt = now.minusSeconds(1),
                   revokedAt = None
                 ))
      _       <- repo.create(session(
                   "10000000-0000-0000-0000-000000000013",
                   "20000000-0000-0000-0000-000000000013",
                   expiresAt = now.plusSeconds(3600),
                   revokedAt = Some(now.minusSeconds(30))
                 ))
      deleted <- repo.deleteExpiredOrRevoked(now)
      kept    <- repo.findActiveByRefreshToken(
                   RefreshToken(UUID.fromString("20000000-0000-0000-0000-000000000011")),
                   now
                 )
      expired <- repo.findActiveByRefreshToken(
                   RefreshToken(UUID.fromString("20000000-0000-0000-0000-000000000012")),
                   now
                 )
      revoked <- repo.findActiveByRefreshToken(
                   RefreshToken(UUID.fromString("20000000-0000-0000-0000-000000000013")),
                   now
                 )
    } yield (deleted, kept, expired, revoked)

    val (deleted, kept, expired, revoked) = result.unsafeRunSync()

    assertEquals(deleted, 2)
    assert(kept.nonEmpty)
    assertEquals(expired, None)
    assertEquals(revoked, None)
  }

  private def session(
      sessionId: String,
      refreshToken: String,
      expiresAt: Instant,
      revokedAt: Option[Instant]
  ): AuthSession =
    AuthSession(
      id = SessionId(UUID.fromString(sessionId)),
      userId = UserId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
      refreshToken = RefreshToken(UUID.fromString(refreshToken)),
      createdAt = Instant.parse("2026-03-30T10:00:00Z"),
      revokedAt = revokedAt,
      expiresAt = expiresAt
    )
}
