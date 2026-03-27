package io.github.oleksiybondar.api.http.routes.rest.auth

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.generic.auto._
import io.github.oleksiybondar.api.testkit.fixtures.AuthServiceFixtures.withAuthService
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import munit.FunSuite
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}

class AuthRoutesSpec extends FunSuite {

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
    assertNotEquals(body.accessToken, "")
    assertNotEquals(body.refreshToken, "")
  }

  test("POST /auth/login returns bad request for invalid credentials") {
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

    assertEquals(response.status, Status.BadRequest)
    assertEquals(body, ErrorResponse("Invalid credentials"))
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
        refreshResponse <- AuthRoutes
                             .routes[IO](ctx.authService)
                             .orNotFound
                             .run(
                               Request[IO](method = Method.POST, uri = uri"/auth/refresh")
                                 .withEntity(RefreshRequest(loginTokens.refreshToken))
                             )
      } yield refreshResponse
    }

    val body = response.as[AuthTokensResponse].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertNotEquals(body.accessToken, "")
    assertNotEquals(body.refreshToken, "")
  }

  test("POST /auth/refresh returns bad request for an invalid refresh token") {
    val response = withAuthService(List(UserFixtures.sampleUser)) { ctx =>
      AuthRoutes
        .routes[IO](ctx.authService)
        .orNotFound
        .run(
          Request[IO](method = Method.POST, uri = uri"/auth/refresh")
            .withEntity(RefreshRequest("missing-refresh-token"))
        )
    }

    val body = response.as[ErrorResponse].unsafeRunSync()

    assertEquals(response.status, Status.BadRequest)
    assertEquals(body, ErrorResponse("Invalid refresh token"))
  }

  test("POST /auth/logout invalidates the provided refresh token") {
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
        logoutResponse  <- AuthRoutes
                             .routes[IO](ctx.authService)
                             .orNotFound
                             .run(
                               Request[IO](method = Method.POST, uri = uri"/auth/logout")
                                 .withEntity(LogoutRequest(loginTokens.refreshToken))
                             )
        refreshResponse <- AuthRoutes
                             .routes[IO](ctx.authService)
                             .orNotFound
                             .run(
                               Request[IO](method = Method.POST, uri = uri"/auth/refresh")
                                 .withEntity(RefreshRequest(loginTokens.refreshToken))
                             )
        refreshBody     <- refreshResponse.as[ErrorResponse]
      } yield (logoutResponse, refreshResponse, refreshBody)
    }

    val (logoutResponse, refreshResponse, refreshBody) = result

    assertEquals(logoutResponse.status, Status.Ok)
    assertEquals(refreshResponse.status, Status.BadRequest)
    assertEquals(refreshBody, ErrorResponse("Invalid refresh token"))
  }
}
