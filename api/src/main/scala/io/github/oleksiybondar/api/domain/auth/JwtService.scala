package io.github.oleksiybondar.api.domain.auth

trait JwtService[F[_]] {
  def encode(claims: AccessTokenClaims): F[AccessToken]
  def decode(token: AccessToken): F[Option[AccessTokenClaims]]
  def verify(token: AccessToken): F[Option[AccessTokenClaims]]
}
