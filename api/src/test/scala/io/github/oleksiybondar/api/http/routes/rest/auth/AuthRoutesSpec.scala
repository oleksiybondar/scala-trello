package io.github.oleksiybondar.api.http.routes.rest.auth

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.testkit.fixtures.AuthServiceFixtures.withAuthService
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import munit.FunSuite
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}

class AuthRoutesSpec extends FunSuite {

  test("POST /auth/register creates tokens for the new user") {
    val response = withAuthService(Nil) { ctx =>
      AuthRoutes
        .routes[IO](ctx.authService)
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
        .routes[IO](ctx.authService)
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

  test("POST /auth/login returns tokens for valid credentials") {
    val response = withAuthService(List(UserFixtures.sampleUser)) { ctx =>
      AuthRoutes
        .routes[IO](ctx.authService)
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
        .routes[IO](ctx.authService)
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
                             .routes[IO](ctx.authService)
                             .orNotFound
                             .run(
                               Request[IO](method = Method.POST, uri = uri"/auth/login")
                                 .withEntity(LoginRequest("alice@example.com", "secret"))
                             )
        loginTokens     <- loginResponse.as[AuthTokensResponse]
        refreshResponse <-
          AuthRoutes
            .routes[IO](ctx.authService)
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
        .routes[IO](ctx.authService)
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
                             .routes[IO](ctx.authService)
                             .orNotFound
                             .run(
                               Request[IO](method = Method.POST, uri = uri"/auth/login")
                                 .withEntity(LoginRequest("alice", "secret"))
                             )
        loginTokens     <- loginResponse.as[AuthTokensResponse]
        logoutResponse  <-
          AuthRoutes
            .routes[IO](ctx.authService)
            .orNotFound
            .run(
              Request[IO](method = Method.POST, uri = uri"/auth/logout")
                .withEntity(
                  LogoutRequest(java.util.UUID.fromString(loginTokens.refresh_token))
                )
            )
        refreshResponse <-
          AuthRoutes
            .routes[IO](ctx.authService)
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
}
