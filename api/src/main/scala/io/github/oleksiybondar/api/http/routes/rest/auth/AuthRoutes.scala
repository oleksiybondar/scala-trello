package io.github.oleksiybondar.api.http.routes.rest.auth

import cats.effect.Async
import io.github.oleksiybondar.api.domain.auth.{
  AuthError,
  AuthService,
  AuthTokens,
  RefreshToken,
  RegisterUserCommand
}
import io.github.oleksiybondar.api.domain.user.Username
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

  val registerEndpoint
      : PublicEndpoint[RegisterRequest, (StatusCode, ErrorResponse), AuthTokensResponse, Any] =
    endpoint.post
      .in("auth" / "register")
      .in(jsonBody[RegisterRequest])
      .errorOut(statusCode.and(jsonBody[ErrorResponse]))
      .out(statusCode(StatusCode.Created))
      .out(jsonBody[AuthTokensResponse])
      .name("register")
      .description(
        "Registers a new user and immediately returns access and refresh tokens. In this project, registration intentionally acts as a login shortcut."
      )
      .tag("auth")

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
    List(registerEndpoint, loginEndpoint, refreshEndpoint, logoutEndpoint)

  def routes[F[_]: Async](authService: AuthService[F]): HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      List(
        registerServerEndpoint(authService),
        loginServerEndpoint(authService),
        refreshServerEndpoint(authService),
        logoutServerEndpoint(authService)
      )
    )

  private def registerServerEndpoint[F[_]: Async](authService: AuthService[F]) =
    registerEndpoint.serverLogic[F](request =>
      authService
        .register(
          RegisterUserCommand(
            email = request.email,
            password = request.password,
            firstName = request.first_name,
            lastName = request.last_name,
            username = request.username.map(value => Username(value.trim)).filter(_.value.nonEmpty)
          )
        )
        .map(authTokensToResponse)
        .leftMap(toRegistrationErrorResponse)
        .value
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
      case AuthError.EmailRequired       => ErrorResponse("Email is required")
      case AuthError.InvalidEmail        => ErrorResponse("Email is invalid")
      case AuthError.EmailAlreadyUsed    => ErrorResponse("Email is already in use")
      case AuthError.WeakPassword(_)     =>
        ErrorResponse("Password does not satisfy the strength requirements")
      case AuthError.InvalidCredentials  => ErrorResponse("Authentication failed")
      case AuthError.InvalidRefreshToken => ErrorResponse("Authentication failed")
    }

  private def toRegistrationErrorResponse(error: AuthError): (StatusCode, ErrorResponse) =
    error match {
      case AuthError.EmailAlreadyUsed => (StatusCode.Conflict, toErrorResponse(error))
      case _                          => (StatusCode.BadRequest, toErrorResponse(error))
    }

  private def authTokensToResponse(tokens: AuthTokens): AuthTokensResponse =
    AuthTokensResponse(
      access_token = tokens.accessToken.value,
      refresh_token = tokens.refreshToken.value.toString,
      token_type = tokens.tokenType,
      expires_in = tokens.expiresIn
    )
}
