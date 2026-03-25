package io.github.oleksiybondar.api.http.middleware

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.auth.AccessToken
import io.github.oleksiybondar.api.testkit.support.InMemoryAuthRepo
import munit.FunSuite
import org.http4s.Headers
import org.http4s.Method
import org.http4s.Request
import org.http4s.Status
import org.http4s.dsl.io.*
import org.http4s.headers.Authorization
import org.http4s.implicits.*
import org.http4s.{AuthScheme, Credentials, HttpRoutes}

class AuthMiddlewareSpec extends FunSuite {

  test("middleware returns unauthorized when the Authorization header is missing") {
    val response = withProtectedRoutes { protectedRoutes =>
      AuthMiddleware
        .middleware[IO](protectedRoutes.authRepo.accessTokens)(protectedRoutes.routes)
        .orNotFound
        .run(Request[IO](method = Method.GET, uri = uri"/protected"))
    }

    val body = response.as[String].unsafeRunSync()

    assertEquals(response.status, Status.Unauthorized)
    assertEquals(body, "Missing or invalid Authorization header")
  }

  test("middleware returns unauthorized when the bearer token is invalid") {
    val response = withProtectedRoutes { protectedRoutes =>
      AuthMiddleware
        .middleware[IO](protectedRoutes.authRepo.accessTokens)(protectedRoutes.routes)
        .orNotFound
        .run(
          Request[IO](method = Method.GET, uri = uri"/protected")
            .putHeaders(
              Authorization(Credentials.Token(AuthScheme.Bearer, "invalid-token"))
            )
        )
    }

    val body = response.as[String].unsafeRunSync()

    assertEquals(response.status, Status.Unauthorized)
    assertEquals(body, "Invalid access token")
  }

  test("middleware allows the request when the bearer token is valid") {
    val response = withProtectedRoutes { protectedRoutes =>
      for {
        _ <- protectedRoutes.authRepo.saveAccessToken(
          AccessToken("valid-token"),
          io.github.oleksiybondar.api.testkit.fixtures.UserFixtures.sampleUser.id
        )
        response <- AuthMiddleware
          .middleware[IO](protectedRoutes.authRepo.accessTokens)(protectedRoutes.routes)
          .orNotFound
          .run(
            Request[IO](method = Method.GET, uri = uri"/protected")
              .putHeaders(
                Authorization(Credentials.Token(AuthScheme.Bearer, "valid-token"))
              )
          )
      } yield response
    }

    val body = response.as[String].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(body, "protected content")
  }

  private final case class ProtectedRoutesContext(
    authRepo: InMemoryAuthRepo[IO],
    routes: HttpRoutes[IO]
  )

  private def withProtectedRoutes[A](run: ProtectedRoutesContext => IO[A]): A =
    (
      for {
        authRepo <- InMemoryAuthRepo.create[IO]()
        routes = HttpRoutes.of[IO] {
          case GET -> Root / "protected" => Ok("protected content")
        }
        result <- run(ProtectedRoutesContext(authRepo, routes))
      } yield result
    ).unsafeRunSync()
}
