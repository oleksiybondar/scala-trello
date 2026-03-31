package io.github.oleksiybondar.api.http.routes.rest.auth

import io.circe.Codec

import java.util.UUID

final case class RegisterRequest(
    email: String,
    password: String,
    first_name: String,
    last_name: String,
    username: Option[String]
)
final case class LoginRequest(login: String, password: String)
final case class RefreshRequest(refresh_token: UUID)
final case class LogoutRequest(refresh_token: UUID)
final case class AuthTokensResponse(
    access_token: String,
    refresh_token: String,
    token_type: String,
    expires_in: Long
)
final case class ErrorResponse(message: String)

given Codec.AsObject[RegisterRequest]    = io.circe.generic.semiauto.deriveCodec
given Codec.AsObject[LoginRequest]       = io.circe.generic.semiauto.deriveCodec
given Codec.AsObject[RefreshRequest]     = io.circe.generic.semiauto.deriveCodec
given Codec.AsObject[LogoutRequest]      = io.circe.generic.semiauto.deriveCodec
given Codec.AsObject[AuthTokensResponse] = io.circe.generic.semiauto.deriveCodec
given Codec.AsObject[ErrorResponse]      = io.circe.generic.semiauto.deriveCodec
