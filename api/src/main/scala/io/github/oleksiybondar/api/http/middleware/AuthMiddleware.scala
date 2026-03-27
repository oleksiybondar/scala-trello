package io.github.oleksiybondar.api.http.middleware

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.auth.{AccessToken, AuthService}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, HttpRoutes, Request, Response, Status}

object AuthMiddleware {

  def middleware[F[_]: Async](
      authService: AuthService[F]
  )(routes: HttpRoutes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] { req =>
      extractAccessToken(req) match {
        case None =>
          Response[F](status = Status.Unauthorized)
            .withEntity("Missing or invalid Authorization header")
            .pure[F]

        case Some(accessToken) =>
          authService.verifyAccessToken(accessToken).flatMap {
            case None =>
              Response[F](status = Status.Unauthorized)
                .withEntity("Invalid access token")
                .pure[F]

            case Some(_) =>
              routes(req).getOrElseF(NotFound())
          }
      }
    }
  }

  private def extractAccessToken[F[_]](request: Request[F]): Option[AccessToken] =
    request.headers.get[Authorization].collect {
      case Authorization(Credentials.Token(AuthScheme.Bearer, token)) =>
        AccessToken(token)
    }
}
