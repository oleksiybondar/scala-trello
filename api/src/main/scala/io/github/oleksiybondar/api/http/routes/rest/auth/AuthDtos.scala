package io.github.oleksiybondar.api.http.routes.rest.auth

final case class LoginRequest(
    login: String,
    password: String
)

final case class RefreshRequest(
    refreshToken: String
)

final case class LogoutRequest(
    refreshToken: String
)

final case class AuthTokensResponse(
    accessToken: String,
    refreshToken: String
)

final case class ErrorResponse(
    message: String
)
