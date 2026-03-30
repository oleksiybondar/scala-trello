package io.github.oleksiybondar.api.domain.auth

import cats.data.EitherT
import io.github.oleksiybondar.api.domain.user.UserId

trait AuthService[F[_]] {
  def login(login: String, password: String): EitherT[F, AuthError, AuthTokens]
  def refresh(refreshToken: RefreshToken): EitherT[F, AuthError, AuthTokens]
  def logout(refreshToken: RefreshToken): F[Unit]
  def verifyToken(accessToken: AccessToken): F[Option[UserId]]
}
