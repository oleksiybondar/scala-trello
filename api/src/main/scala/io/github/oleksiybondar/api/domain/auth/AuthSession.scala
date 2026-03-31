package io.github.oleksiybondar.api.domain.auth

import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant
import java.util.UUID

final case class SessionId(value: UUID)     extends AnyVal
final case class AccessToken(value: String) extends AnyVal
final case class RefreshToken(value: UUID)  extends AnyVal

final case class AuthSession(
    id: SessionId,
    userId: UserId,
    refreshToken: RefreshToken,
    createdAt: Instant,
    revokedAt: Option[Instant],
    expiresAt: Instant
)

final case class AccessTokenClaims(
    userId: UserId,
    sessionId: SessionId,
    tokenId: UUID,
    issuedAt: Instant,
    expiresAt: Instant
)

final case class AuthTokens(
    accessToken: AccessToken,
    refreshToken: RefreshToken,
    tokenType: String,
    expiresIn: Long
)
