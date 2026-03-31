package io.github.oleksiybondar.api.http.routes.rest.auth

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.auth.{AccessTokenClaims, SessionId}
import io.github.oleksiybondar.api.domain.user.UserServiceLive
import io.github.oleksiybondar.api.testkit.fixtures.AuthServiceFixtures.{
  fakePasswordHasher,
  passwordStrengthValidator,
  unsafeEmptyPasswordHistory,
  withAuthService
}
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import munit.FunSuite
import org.http4s.circe.CirceEntityCodec._
import org.http4s.headers.Authorization
import org.http4s.implicits._
import org.http4s.{AuthScheme, Credentials, Method, Request, Status}

class AuthRoutesSpec extends FunSuite {

  test("POST /auth/register creates tokens for the new user") {
    val response = withAuthService(Nil) { ctx =>
      AuthRoutes
        .publicRoutes[IO](ctx.authService)
        .orNotFound
        .run(
          Request[IO](method = Method.POST, uri = uri"/auth/register")
            .withEntity(
              RegisterRequest(
                email = "alice@example.com",
                password = "secret123",
                first_name = "Alice",
                last_name = "Example",
                username = Some("alice")
              )
            )
        )
    }

    val body = response.as[AuthTokensResponse].unsafeRunSync()

    assertEquals(response.status, Status.Created)
    assertNotEquals(body.access_token, "")
    assertNotEquals(body.refresh_token, "")
    assertEquals(body.token_type, "Bearer")
    assertEquals(body.expires_in, 900L)
  }

  test("POST /auth/register returns conflict when the email is already used") {
    val response = withAuthService(List(UserFixtures.sampleUser)) { ctx =>
      AuthRoutes
        .publicRoutes[IO](ctx.authService)
        .orNotFound
        .run(
          Request[IO](method = Method.POST, uri = uri"/auth/register")
            .withEntity(
              RegisterRequest(
                email = "alice@example.com",
                password = "secret123",
                first_name = "Alice",
                last_name = "Example",
                username = Some("alice")
              )
            )
        )
    }

    val body = response.as[ErrorResponse].unsafeRunSync()

    assertEquals(response.status, Status.Conflict)
    assertEquals(body, ErrorResponse("Email is already in use"))
  }

  test("POST /auth/register returns bad request for a malformed email") {
    val response = withAuthService(Nil) { ctx =>
      AuthRoutes
        .publicRoutes[IO](ctx.authService)
        .orNotFound
        .run(
          Request[IO](method = Method.POST, uri = uri"/auth/register")
            .withEntity(
              RegisterRequest(
                email = "not-an-email",
                password = "secret123",
                first_name = "Alice",
                last_name = "Example",
                username = Some("alice")
              )
            )
        )
    }

    val body = response.as[ErrorResponse].unsafeRunSync()

    assertEquals(response.status, Status.BadRequest)
    assertEquals(body, ErrorResponse("Email is invalid"))
  }

  test("POST /auth/register returns bad request when both email and username are missing") {
    val response = withAuthService(Nil) { ctx =>
      AuthRoutes
        .publicRoutes[IO](ctx.authService)
        .orNotFound
        .run(
          Request[IO](method = Method.POST, uri = uri"/auth/register")
            .withEntity(
              RegisterRequest(
                email = "   ",
                password = "secret123",
                first_name = "Alice",
                last_name = "Example",
                username = None
              )
            )
        )
    }

    val body = response.as[ErrorResponse].unsafeRunSync()

    assertEquals(response.status, Status.BadRequest)
    assertEquals(body, ErrorResponse("Email is required"))
  }

  test("POST /auth/login returns tokens for valid credentials") {
    val response = withAuthService(List(UserFixtures.sampleUser)) { ctx =>
      AuthRoutes
        .publicRoutes[IO](ctx.authService)
        .orNotFound
        .run(
          Request[IO](method = Method.POST, uri = uri"/auth/login")
            .withEntity(LoginRequest("alice", "secret"))
        )
    }

    val body = response.as[AuthTokensResponse].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertNotEquals(body.access_token, "")
    assertNotEquals(body.refresh_token, "")
    assertEquals(body.token_type, "Bearer")
    assertEquals(body.expires_in, 900L)
  }

  test("POST /auth/login returns unauthorized for invalid credentials") {
    val response = withAuthService(Nil) { ctx =>
      AuthRoutes
        .publicRoutes[IO](ctx.authService)
        .orNotFound
        .run(
          Request[IO](method = Method.POST, uri = uri"/auth/login")
            .withEntity(LoginRequest("missing-user", "secret"))
        )
    }

    val body = response.as[ErrorResponse].unsafeRunSync()

    assertEquals(response.status, Status.Unauthorized)
    assertEquals(body, ErrorResponse("Authentication failed"))
  }

  test("POST /auth/refresh returns new tokens for a valid refresh token") {
    val response = withAuthService(List(UserFixtures.sampleUser)) { ctx =>
      for {
        loginResponse   <- AuthRoutes
                             .publicRoutes[IO](ctx.authService)
                             .orNotFound
                             .run(
                               Request[IO](method = Method.POST, uri = uri"/auth/login")
                                 .withEntity(LoginRequest("alice@example.com", "secret"))
                             )
        loginTokens     <- loginResponse.as[AuthTokensResponse]
        refreshResponse <-
          AuthRoutes
            .publicRoutes[IO](ctx.authService)
            .orNotFound
            .run(
              Request[IO](method = Method.POST, uri = uri"/auth/refresh")
                .withEntity(
                  RefreshRequest(java.util.UUID.fromString(loginTokens.refresh_token))
                )
            )
      } yield refreshResponse
    }

    val body = response.as[AuthTokensResponse].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertNotEquals(body.access_token, "")
    assertNotEquals(body.refresh_token, "")
    assertEquals(body.token_type, "Bearer")
    assertEquals(body.expires_in, 900L)
  }

  test("POST /auth/refresh returns unauthorized for an invalid refresh token") {
    val response = withAuthService(List(UserFixtures.sampleUser)) { ctx =>
      AuthRoutes
        .publicRoutes[IO](ctx.authService)
        .orNotFound
        .run(
          Request[IO](method = Method.POST, uri = uri"/auth/refresh")
            .withEntity(RefreshRequest(java.util.UUID.randomUUID()))
        )
    }

    val body = response.as[ErrorResponse].unsafeRunSync()

    assertEquals(response.status, Status.Unauthorized)
    assertEquals(body, ErrorResponse("Authentication failed"))
  }

  test("POST /auth/logout invalidates the provided refresh token and returns no content") {
    val result = withAuthService(List(UserFixtures.sampleUser)) { ctx =>
      for {
        loginResponse   <- AuthRoutes
                             .publicRoutes[IO](ctx.authService)
                             .orNotFound
                             .run(
                               Request[IO](method = Method.POST, uri = uri"/auth/login")
                                 .withEntity(LoginRequest("alice", "secret"))
                             )
        loginTokens     <- loginResponse.as[AuthTokensResponse]
        logoutResponse  <-
          AuthRoutes
            .publicRoutes[IO](ctx.authService)
            .orNotFound
            .run(
              Request[IO](method = Method.POST, uri = uri"/auth/logout")
                .withEntity(
                  LogoutRequest(java.util.UUID.fromString(loginTokens.refresh_token))
                )
            )
        refreshResponse <-
          AuthRoutes
            .publicRoutes[IO](ctx.authService)
            .orNotFound
            .run(
              Request[IO](method = Method.POST, uri = uri"/auth/refresh")
                .withEntity(
                  RefreshRequest(java.util.UUID.fromString(loginTokens.refresh_token))
                )
            )
        refreshBody     <- refreshResponse.as[ErrorResponse]
      } yield (logoutResponse, refreshResponse, refreshBody)
    }

    val (logoutResponse, refreshResponse, refreshBody) = result

    assertEquals(logoutResponse.status, Status.NoContent)
    assertEquals(refreshResponse.status, Status.Unauthorized)
    assertEquals(refreshBody, ErrorResponse("Authentication failed"))
  }

  test("GET /auth/me returns the current authenticated user") {
    val result = withAuthService(List(UserFixtures.sampleUser)) { ctx =>
      val userService = new UserServiceLive[IO](
        ctx.userRepo,
        fakePasswordHasher,
        passwordStrengthValidator,
        unsafeEmptyPasswordHistory
      )

      for {
        loginResponse <- AuthRoutes
                           .publicRoutes[IO](ctx.authService)
                           .orNotFound
                           .run(
                             Request[IO](method = Method.POST, uri = uri"/auth/login")
                               .withEntity(LoginRequest("alice", "secret"))
                           )
        tokens        <- loginResponse.as[AuthTokensResponse]
        meResponse    <- AuthRoutes
                           .privateRoutes[IO](ctx.authService, userService)
                           .orNotFound
                           .run(
                             Request[IO](method = Method.GET, uri = uri"/auth/me")
                               .putHeaders(
                                 Authorization(
                                   Credentials.Token(AuthScheme.Bearer, tokens.access_token)
                                 )
                               )
                           )
        body          <- meResponse.as[CurrentUserResponse]
      } yield (meResponse, body)
    }

    val (response, body) = result

    assertEquals(response.status, Status.Ok)
    assertEquals(body.id, UserFixtures.sampleUser.id.value.toString)
    assertEquals(body.username, Some("alice"))
    assertEquals(body.email, Some("alice@example.com"))
    assertEquals(body.first_name, "Alice")
    assertEquals(body.last_name, "Example")
    assertEquals(body.avatar_url, None)
    assertEquals(body.created_at, "2026-03-25T10:15:30Z")
  }

  test("GET /auth/me returns unauthorized when the bearer token is missing") {
    val response = withAuthService(List(UserFixtures.sampleUser)) { ctx =>
      val userService = new UserServiceLive[IO](
        ctx.userRepo,
        fakePasswordHasher,
        passwordStrengthValidator,
        unsafeEmptyPasswordHistory
      )

      AuthRoutes
        .privateRoutes[IO](ctx.authService, userService)
        .orNotFound
        .run(Request[IO](method = Method.GET, uri = uri"/auth/me"))
    }

    val body = response.bodyText.compile.string.unsafeRunSync()

    assertEquals(response.status, Status.Unauthorized)
    assertEquals(body, "Missing or invalid Authorization header")
  }

  test("GET /auth/me returns not found when the token is valid but the user does not exist") {
    val result = withAuthService(Nil) { ctx =>
      val userService = new UserServiceLive[IO](
        ctx.userRepo,
        fakePasswordHasher,
        passwordStrengthValidator,
        unsafeEmptyPasswordHistory
      )

      for {
        now      <- IO.realTimeInstant
        token    <- ctx.jwtService.encode(
                      AccessTokenClaims(
                        userId = UserFixtures.sampleUser.id,
                        sessionId = SessionId(java.util.UUID.randomUUID()),
                        tokenId = java.util.UUID.randomUUID(),
                        issuedAt = now,
                        expiresAt = now.plusSeconds(900)
                      )
                    )
        response <- AuthRoutes
                      .privateRoutes[IO](ctx.authService, userService)
                      .orNotFound
                      .run(
                        Request[IO](method = Method.GET, uri = uri"/auth/me")
                          .putHeaders(
                            Authorization(
                              Credentials.Token(AuthScheme.Bearer, token.value)
                            )
                          )
                      )
        body     <- response.as[ErrorResponse]
      } yield (response, body)
    }

    val (response, body) = result

    assertEquals(response.status, Status.NotFound)
    assertEquals(body, ErrorResponse("Current user was not found"))
  }
}
