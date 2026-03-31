package io.github.oleksiybondar.api.domain.auth

import cats.data.EitherT
import io.github.oleksiybondar.api.domain.auth.password.PasswordStrengthError
import io.github.oleksiybondar.api.domain.user.{UserId, Username}

final case class RegisterUserCommand(
    email: String,
    password: String,
    firstName: String,
    lastName: String,
    username: Option[Username]
)

trait AuthService[F[_]] {
  def register(command: RegisterUserCommand): EitherT[F, AuthError, AuthTokens]
  def login(login: String, password: String): EitherT[F, AuthError, AuthTokens]
  def refresh(refreshToken: RefreshToken): EitherT[F, AuthError, AuthTokens]
  def logout(refreshToken: RefreshToken): F[Unit]
  def verifyToken(accessToken: AccessToken): F[Option[UserId]]
}

sealed trait AuthError extends Product with Serializable

object AuthError {
  case object InvalidCredentials                                     extends AuthError
  case object InvalidRefreshToken                                    extends AuthError
  case object EmailRequired                                          extends AuthError
  case object InvalidEmail                                           extends AuthError
  case object EmailAlreadyUsed                                       extends AuthError
  final case class WeakPassword(errors: List[PasswordStrengthError]) extends AuthError
}
