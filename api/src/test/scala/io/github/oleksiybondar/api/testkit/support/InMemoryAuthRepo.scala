package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all.*
import io.github.oleksiybondar.api.domain.auth.{AccessToken, RefreshToken, TokenRepo}
import io.github.oleksiybondar.api.domain.user.UserId

final class InMemoryAuthRepo[F[_]: Sync] private (
  val accessTokens: Ref[F, Map[AccessToken, UserId]],
  val refreshTokens: Ref[F, Map[RefreshToken, UserId]]
) extends TokenRepo[F] {

  override def saveAccessToken(token: AccessToken, userId: UserId): F[Unit] =
    accessTokens.update(_ + (token -> userId))

  override def saveRefreshToken(token: RefreshToken, userId: UserId): F[Unit] =
    refreshTokens.update(_ + (token -> userId))

  override def findUserIdByAccessToken(token: AccessToken): F[Option[UserId]] =
    accessTokens.get.map(_.get(token))

  override def findUserIdByRefreshToken(token: RefreshToken): F[Option[UserId]] =
    refreshTokens.get.map(_.get(token))

  override def rotateRefreshToken(current: RefreshToken, next: RefreshToken, userId: UserId): F[Unit] =
    refreshTokens.update(_ - current + (next -> userId))

  override def deleteRefreshToken(token: RefreshToken): F[Unit] =
    refreshTokens.update(_ - token)
}

object InMemoryAuthRepo {

  def create[F[_]: Sync](): F[InMemoryAuthRepo[F]] =
    (
      Ref.of[F, Map[AccessToken, UserId]](Map.empty),
      Ref.of[F, Map[RefreshToken, UserId]](Map.empty)
    ).mapN(new InMemoryAuthRepo[F](_, _))
}
