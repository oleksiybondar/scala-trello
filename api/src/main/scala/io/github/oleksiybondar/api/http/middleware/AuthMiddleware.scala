package io.github.oleksiybondar.api.http.middleware

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.auth.{AccessToken, AuthService}
import io.github.oleksiybondar.api.domain.user.UserId
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, HttpRoutes, Request, Response, Status}

object AuthMiddleware {

  def middleware[F[_]: Async](
      authService: AuthService[F]
  )(routes: HttpRoutes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    def unauthorized(message: String): F[Response[F]] =
      Response[F](status = Status.Unauthorized)
        .withEntity(message)
        .pure[F]

    HttpRoutes.of[F] { req =>
      extractAccessToken(req) match {
        case None =>
          unauthorized("Missing or invalid Authorization header")

        case Some(accessToken) =>
          authenticate(authService, accessToken).flatMap {
            case None =>
              unauthorized("Invalid access token")

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

  private def authenticate[F[_]](
      authService: AuthService[F],
      accessToken: AccessToken
  ): F[Option[UserId]] =
    authService.verifyToken(accessToken)
}
