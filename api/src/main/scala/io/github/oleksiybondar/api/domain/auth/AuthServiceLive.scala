package io.github.oleksiybondar.api.domain.auth

import cats.effect.kernel.Sync
import cats.syntax.all.*
import io.github.oleksiybondar.api.domain.user.{Email, Username, UserId}
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo

import java.util.UUID

final class AuthServiceLive[F[_]: Sync](
  userRepo: UserRepo[F],
  tokenRepo: TokenRepo[F]
) extends AuthService[F] {

  override def login(command: LoginCommand): F[Option[AuthTokens]] = {
    val userLookup =
      if (command.login.contains("@"))
        userRepo.findByEmail(Email(command.login))
      else
        userRepo.findByUsername(Username(command.login))

    userLookup.flatMap {
      case None =>
        none[AuthTokens].pure[F]

      case Some(user) =>
        for {
          accessToken <- generateAccessToken
          refreshToken <- generateRefreshToken
          _ <- tokenRepo.saveAccessToken(accessToken, user.id)
          _ <- tokenRepo.saveRefreshToken(refreshToken, user.id)
        } yield AuthTokens(accessToken, refreshToken).some
    }
  }

  override def refresh(command: RefreshTokenCommand): F[Option[AuthTokens]] =
    tokenRepo.findUserIdByRefreshToken(command.refreshToken).flatMap {
      case None =>
        none[AuthTokens].pure[F]

      case Some(userId) =>
        for {
          accessToken <- generateAccessToken
          newRefreshToken <- generateRefreshToken
          _ <- tokenRepo.saveAccessToken(accessToken, userId)
          _ <- tokenRepo.rotateRefreshToken(command.refreshToken, newRefreshToken, userId)
        } yield AuthTokens(accessToken, newRefreshToken).some
    }

  override def logout(command: LogoutCommand): F[Unit] =
    tokenRepo.deleteRefreshToken(command.refreshToken)

  override def verifyAccessToken(accessToken: AccessToken): F[Option[UserId]] =
    tokenRepo.findUserIdByAccessToken(accessToken)

  private def generateAccessToken: F[AccessToken] =
    Sync[F].delay(AccessToken(UUID.randomUUID().toString))

  private def generateRefreshToken: F[RefreshToken] =
    Sync[F].delay(RefreshToken(UUID.randomUUID().toString))
}
