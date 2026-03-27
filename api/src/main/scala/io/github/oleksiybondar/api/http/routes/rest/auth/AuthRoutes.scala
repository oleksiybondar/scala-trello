package io.github.oleksiybondar.api.http.routes.rest.auth

import cats.effect.Async
import cats.syntax.all._
import io.circe.generic.auto._
import io.github.oleksiybondar.api.domain.auth._
import io.github.oleksiybondar.api.http.routes.rest.auth._
import org.http4s.HttpRoutes
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter

object AuthRoutes {

  // ----- endpoint descriptions -----

  private val base =
    endpoint
      .in("auth")
      .tag("auth")

  val loginEndpoint: PublicEndpoint[LoginRequest, ErrorResponse, AuthTokensResponse, Any] =
    base.post
      .in("login")
      .in(jsonBody[LoginRequest])
      .errorOut(jsonBody[ErrorResponse])
      .out(jsonBody[AuthTokensResponse])
      .name("login")
      .description("Authenticate user with username/email and password")

  val refreshEndpoint: PublicEndpoint[RefreshRequest, ErrorResponse, AuthTokensResponse, Any] =
    base.post
      .in("refresh")
      .in(jsonBody[RefreshRequest])
      .errorOut(jsonBody[ErrorResponse])
      .out(jsonBody[AuthTokensResponse])
      .name("refresh")
      .description("Refresh access token using refresh token")

  val logoutEndpoint: PublicEndpoint[LogoutRequest, ErrorResponse, Unit, Any] =
    base.post
      .in("logout")
      .in(jsonBody[LogoutRequest])
      .errorOut(jsonBody[ErrorResponse])
      .out(emptyOutput)
      .name("logout")
      .description("Logout user and invalidate refresh token")

  val all =
    List(loginEndpoint, refreshEndpoint, logoutEndpoint)

  // ----- routes -----

  def routes[F[_]: Async](authService: AuthService[F]): HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      List(
        loginServerEndpoint(authService),
        refreshServerEndpoint(authService),
        logoutServerEndpoint(authService)
      )
    )

  // ----- server endpoints -----

  private def loginServerEndpoint[F[_]: Async](authService: AuthService[F]) =
    loginEndpoint.serverLogic(loginLogic(authService))

  private def refreshServerEndpoint[F[_]: Async](authService: AuthService[F]) =
    refreshEndpoint.serverLogic(refreshLogic(authService))

  private def logoutServerEndpoint[F[_]: Async](authService: AuthService[F]) =
    logoutEndpoint.serverLogic(logoutLogic(authService))

  // ----- server logic -----

  private def loginLogic[F[_]: Async](
      authService: AuthService[F]
  )(request: LoginRequest): F[Either[ErrorResponse, AuthTokensResponse]] =
    authService
      .login(toLoginCommand(request))
      .map {
        case Some(tokens) => Right(toResponse(tokens))
        case None         => Left(ErrorResponse("Invalid credentials"))
      }

  private def refreshLogic[F[_]: Async](
      authService: AuthService[F]
  )(request: RefreshRequest): F[Either[ErrorResponse, AuthTokensResponse]] =
    authService
      .refresh(toRefreshCommand(request))
      .map {
        case Some(tokens) => Right(toResponse(tokens))
        case None         => Left(ErrorResponse("Invalid refresh token"))
      }

  private def logoutLogic[F[_]: Async](
      authService: AuthService[F]
  )(request: LogoutRequest): F[Either[ErrorResponse, Unit]] =
    authService
      .logout(toLogoutCommand(request))
      .as(Right(()))

  // ----- transport <-> domain mapping -----

  private def toLoginCommand(request: LoginRequest): LoginCommand =
    LoginCommand(
      login = request.login,
      password = request.password
    )

  private def toRefreshCommand(request: RefreshRequest): RefreshTokenCommand =
    RefreshTokenCommand(
      refreshToken = RefreshToken(request.refreshToken)
    )

  private def toLogoutCommand(request: LogoutRequest): LogoutCommand =
    LogoutCommand(
      refreshToken = RefreshToken(request.refreshToken)
    )

  private def toResponse(tokens: AuthTokens): AuthTokensResponse =
    AuthTokensResponse(
      accessToken = tokens.accessToken.value,
      refreshToken = tokens.refreshToken.value
    )
}
