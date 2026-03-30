package io.github.oleksiybondar.api.http.routes.rest.auth

import cats.effect.Async
import io.github.oleksiybondar.api.domain.auth.{AuthError, AuthService, AuthTokens, RefreshToken}
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.{
  AnyEndpoint,
  PublicEndpoint,
  endpoint,
  oneOf,
  oneOfVariant,
  statusCode,
  stringToPath
}

object AuthRoutes {

  private val unauthorizedOutput =
    oneOf[ErrorResponse](
      oneOfVariant(
        statusCode(StatusCode.Unauthorized)
          .and(jsonBody[ErrorResponse])
      )
    )

  val loginEndpoint: PublicEndpoint[LoginRequest, ErrorResponse, AuthTokensResponse, Any] =
    endpoint.post
      .in("auth" / "login")
      .in(jsonBody[LoginRequest])
      .errorOut(unauthorizedOutput)
      .out(jsonBody[AuthTokensResponse])
      .name("login")
      .description(
        "Bootstrap login: finds a user by username or email and returns access and refresh tokens. The password field is not verified yet."
      )
      .tag("auth")

  val refreshEndpoint: PublicEndpoint[RefreshRequest, ErrorResponse, AuthTokensResponse, Any] =
    endpoint.post
      .in("auth" / "refresh")
      .in(jsonBody[RefreshRequest])
      .errorOut(unauthorizedOutput)
      .out(jsonBody[AuthTokensResponse])
      .name("refresh")
      .description("Rotates the refresh token and returns a new access and refresh token pair")
      .tag("auth")

  val logoutEndpoint: PublicEndpoint[LogoutRequest, Unit, Unit, Any] =
    endpoint.post
      .in("auth" / "logout")
      .in(jsonBody[LogoutRequest])
      .out(statusCode(StatusCode.NoContent))
      .name("logout")
      .description("Revokes the session associated with the refresh token")
      .tag("auth")

  val all: List[AnyEndpoint] =
    List(loginEndpoint, refreshEndpoint, logoutEndpoint)

  def routes[F[_]: Async](authService: AuthService[F]): HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      List(
        loginServerEndpoint(authService),
        refreshServerEndpoint(authService),
        logoutServerEndpoint(authService)
      )
    )

  private def loginServerEndpoint[F[_]: Async](authService: AuthService[F]) =
    loginEndpoint.serverLogic[F](request =>
      authService
        .login(request.login, request.password)
        .map(tokens => authTokensToResponse(tokens))
        .leftMap(toErrorResponse)
        .value
    )

  private def refreshServerEndpoint[F[_]: Async](authService: AuthService[F]) =
    refreshEndpoint.serverLogic[F](request =>
      authService
        .refresh(RefreshToken(request.refresh_token))
        .map(tokens => authTokensToResponse(tokens))
        .leftMap(toErrorResponse)
        .value
    )

  private def logoutServerEndpoint[F[_]: Async](authService: AuthService[F]) =
    logoutEndpoint.serverLogicSuccess[F](request =>
      authService.logout(RefreshToken(request.refresh_token))
    )

  private def toErrorResponse(error: AuthError): ErrorResponse =
    error match {
      case AuthError.InvalidCredentials  => ErrorResponse("Authentication failed")
      case AuthError.InvalidRefreshToken => ErrorResponse("Authentication failed")
    }

  private def authTokensToResponse(tokens: AuthTokens): AuthTokensResponse =
    AuthTokensResponse(
      access_token = tokens.accessToken.value,
      refresh_token = tokens.refreshToken.value.toString,
      token_type = tokens.tokenType,
      expires_in = tokens.expiresIn
    )
}
