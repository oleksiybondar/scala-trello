package io.github.oleksiybondar.api.domain.auth

trait AuthService[F[_]] {
  def login(command: LoginCommand): F[Option[AuthTokens]]
  def refresh(command: RefreshTokenCommand): F[Option[AuthTokens]]
  def logout(command: LogoutCommand): F[Unit]
}

final case class LoginCommand(
                               login: String,
                               password: String
                             )

final case class RefreshTokenCommand(
                                      refreshToken: RefreshToken
                                    )

final case class LogoutCommand(
                                refreshToken: RefreshToken
                              )