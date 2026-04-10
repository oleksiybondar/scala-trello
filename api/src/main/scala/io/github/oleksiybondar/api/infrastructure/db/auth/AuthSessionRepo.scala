package io.github.oleksiybondar.api.infrastructure.db.auth

import io.github.oleksiybondar.api.domain.auth.{AuthSession, RefreshToken, SessionId}

import java.time.Instant

trait AuthSessionRepo[F[_]] {
  def create(session: AuthSession): F[Unit]

  def findActiveByRefreshToken(refreshToken: RefreshToken, now: Instant): F[Option[AuthSession]]

  def deleteExpiredOrRevoked(now: Instant): F[Int]

  def rotateRefreshToken(
      sessionId: SessionId,
      currentRefreshToken: RefreshToken,
      nextRefreshToken: RefreshToken,
      now: Instant
  ): F[Boolean]

  def revokeByRefreshToken(refreshToken: RefreshToken, revokedAt: Instant): F[Boolean]
}
