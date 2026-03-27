package io.github.oleksiybondar.api.domain.auth

import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant

enum TokenType {
  case Access
  case Refresh
}

final case class StoredToken[T](
    token: T,
    userId: UserId,
    tokenType: TokenType,
    createdAt: Instant
)

trait TokenRepo[F[_]] {
  def saveAccessToken(token: AccessToken, userId: UserId): F[Unit]
  def saveRefreshToken(token: RefreshToken, userId: UserId): F[Unit]
  def findUserIdByAccessToken(token: AccessToken): F[Option[UserId]]
  def findUserIdByRefreshToken(token: RefreshToken): F[Option[UserId]]
  def rotateRefreshToken(current: RefreshToken, next: RefreshToken, userId: UserId): F[Unit]
  def deleteRefreshToken(token: RefreshToken): F[Unit]
}
