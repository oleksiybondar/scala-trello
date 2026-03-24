package io.github.oleksiybondar.api.http.middleware

import cats.effect.Async
import cats.effect.Ref
import cats.syntax.all.*
import io.github.oleksiybondar.api.domain.auth.AccessToken
import io.github.oleksiybondar.api.domain.user.UserId
import org.http4s.{AuthScheme, Credentials, HttpRoutes, Request, Response, Status}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization

object AuthMiddleware {

  def middleware[F[_]: Async](
                               accessTokenStore: Ref[F, Map[AccessToken, UserId]]
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
          accessTokenStore.get.flatMap { store =>
            store.get(accessToken) match {
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
  }

  private def extractAccessToken[F[_]](request: Request[F]): Option[AccessToken] =
    request.headers.get[Authorization].collect {
      case Authorization(Credentials.Token(AuthScheme.Bearer, token)) =>
        AccessToken(token)
    }
}