package io.github.oleksiybondar.api.http.middleware

import cats.data.EitherT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.auth.{
  AccessToken,
  AuthError,
  AuthService,
  AuthTokens,
  RefreshToken
}
import io.github.oleksiybondar.api.domain.user.UserId
import munit.FunSuite
import org.http4s.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.implicits._
import org.http4s.{AuthScheme, Credentials, HttpRoutes, Method, Request, Status}

import java.util.UUID

class AuthMiddlewareSpec extends FunSuite {

  test("middleware returns unauthorized when the Authorization header is missing") {
    val response =
      runRequest(TestAuthService(_ => IO.pure(None)), Request[IO](Method.GET, uri"/protected"))

    val body = response.as[String].unsafeRunSync()

    assertEquals(response.status, Status.Unauthorized)
    assertEquals(body, "Missing or invalid Authorization header")
  }

  test("middleware returns unauthorized when the bearer token is invalid") {
    val response = runRequest(
      TestAuthService(_ => IO.pure(None)),
      authorizedRequest("invalid-token")
    )

    val body = response.as[String].unsafeRunSync()

    assertEquals(response.status, Status.Unauthorized)
    assertEquals(body, "Invalid access token")
  }

  test("middleware allows the request when the bearer token is valid") {
    val response = runRequest(
      TestAuthService(_ => IO.pure(Some(UserId(UUID.randomUUID())))),
      authorizedRequest("valid-token")
    )

    val body = response.as[String].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(body, "protected content")
  }

  private def authorizedRequest(token: String): Request[IO] =
    Request[IO](method = Method.GET, uri = uri"/protected")
      .putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token)))

  private def runRequest(authService: AuthService[IO], request: Request[IO]) =
    AuthMiddleware
      .middleware[IO](authService)(protectedRoutes)
      .orNotFound
      .run(request)
      .unsafeRunSync()

  private val protectedRoutes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "protected" => Ok("protected content")
    }

  private final case class TestAuthService(
      verify: AccessToken => IO[Option[UserId]]
  ) extends AuthService[IO] {
    override def login(login: String, password: String): EitherT[IO, AuthError, AuthTokens] =
      EitherT.leftT(AuthError.InvalidCredentials)

    override def refresh(refreshToken: RefreshToken): EitherT[IO, AuthError, AuthTokens] =
      EitherT.leftT(AuthError.InvalidRefreshToken)

    override def logout(refreshToken: RefreshToken): IO[Unit] =
      IO.unit

    override def verifyToken(accessToken: AccessToken): IO[Option[UserId]] =
      verify(accessToken)
  }
}
