package io.github.oleksiybondar.api.domain.auth

final case class AccessToken(value: String) extends AnyVal
final case class RefreshToken(value: String) extends AnyVal

final case class AuthTokens(
                             accessToken: AccessToken,
                             refreshToken: RefreshToken
                           )