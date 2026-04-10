package io.github.oleksiybondar.api.http.routes.rest.auth

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.auth.{
  AuthError,
  AuthService,
  AuthTokens,
  RefreshToken,
  RegisterUserCommand
}
import io.github.oleksiybondar.api.domain.user.{User, UserService, Username}
import io.github.oleksiybondar.api.http.TapirSupport
import io.github.oleksiybondar.api.http.middleware.AuthMiddleware
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response, Status}
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.{
  AnyEndpoint,
  Endpoint,
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

  val currentUserEndpoint: Endpoint[String, Unit, ErrorResponse, CurrentUserResponse, Any] =
    endpoint.get
      .in("auth" / "me")
      .securityIn(
        sttp.tapir.auth.bearer[String]()
          .description("Access token for the current authenticated user")
      )
      .errorOut(jsonBody[ErrorResponse])
      .out(jsonBody[CurrentUserResponse])
      .name("currentUser")
      .description("Returns the current authenticated user")
      .tag("auth")

  val all: List[AnyEndpoint] =
    List(registerEndpoint, loginEndpoint, refreshEndpoint, logoutEndpoint, currentUserEndpoint)

  def publicRoutes[F[_]: Async](authService: AuthService[F]): HttpRoutes[F] =
    TapirSupport.interpreter[F].toRoutes(
      List(
        registerServerEndpoint(authService),
        loginServerEndpoint(authService),
        refreshServerEndpoint(authService),
        logoutServerEndpoint(authService)
      )
    )

  def privateRoutes[F[_]: Async](
      authService: AuthService[F],
      userService: UserService[F]
  ): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] { case req @ GET -> Root / "auth" / "me" =>
      AuthMiddleware.middleware[F](authService) {
        HttpRoutes.of[F] { case authedReq @ GET -> Root / "auth" / "me" =>
          AuthMiddleware.extractAuthenticatedUserId(authedReq) match {
            case Some(userId) =>
              userService.getUser(userId).flatMap {
                case Some(user) => Ok(currentUserToResponse(user))
                case None       => NotFound(ErrorResponse("Current user was not found"))
              }
            case None         =>
              Response(status = Status.Unauthorized)
                .withEntity(ErrorResponse("Authentication context is missing"))
                .pure[F]
          }
        }
      }.orNotFound(req)
    }
  }

  def routes[F[_]: Async](authService: AuthService[F], userService: UserService[F]): HttpRoutes[F] =
    publicRoutes(authService) <+> privateRoutes(authService, userService)

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

  private def currentUserToResponse(user: User): CurrentUserResponse =
    CurrentUserResponse(
      id = user.id.value.toString,
      username = user.username.map(_.value),
      email = user.email.map(_.value),
      first_name = user.firstName.value,
      last_name = user.lastName.value,
      avatar_url = user.avatarUrl.map(_.value),
      created_at = user.createdAt.toString
    )
}
