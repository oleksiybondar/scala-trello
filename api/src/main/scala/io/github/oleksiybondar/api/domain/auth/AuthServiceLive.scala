package io.github.oleksiybondar.api.domain.auth

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all.*
import io.github.oleksiybondar.api.domain.user.{Email, Username, UserId}
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo

import java.util.UUID

final class AuthServiceLive[F[_]: Sync](
                                         userRepo: UserRepo[F],
                                         accessTokenStore: Ref[F, Map[AccessToken, UserId]],
                                         refreshTokenStore: Ref[F, Map[RefreshToken, UserId]]
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
          _ <- accessTokenStore.update(_ + (accessToken -> user.id))
          _ <- refreshTokenStore.update(_ + (refreshToken -> user.id))
        } yield AuthTokens(accessToken, refreshToken).some
    }
  }

  override def refresh(command: RefreshTokenCommand): F[Option[AuthTokens]] =
    refreshTokenStore.get.flatMap { tokens =>
      tokens.get(command.refreshToken) match {
        case None =>
          none[AuthTokens].pure[F]

        case Some(userId) =>
          for {
            accessToken <- generateAccessToken
            newRefreshToken <- generateRefreshToken
            _ <- accessTokenStore.update(_ + (accessToken -> userId))
            _ <- refreshTokenStore.update { current =>
              current - command.refreshToken + (newRefreshToken -> userId)
            }
          } yield AuthTokens(accessToken, newRefreshToken).some
      }
    }

  override def logout(command: LogoutCommand): F[Unit] =
    refreshTokenStore.update(_ - command.refreshToken)

  private def generateAccessToken: F[AccessToken] =
    Sync[F].delay(AccessToken(UUID.randomUUID().toString))

  private def generateRefreshToken: F[RefreshToken] =
    Sync[F].delay(RefreshToken(UUID.randomUUID().toString))
}