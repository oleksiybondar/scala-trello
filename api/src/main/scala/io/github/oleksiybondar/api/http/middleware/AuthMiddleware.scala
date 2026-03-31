package io.github.oleksiybondar.api.http.middleware

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.auth.{AccessToken, AuthService}
import io.github.oleksiybondar.api.domain.user.UserId
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, Header, HttpRoutes, Request, Response, Status}
import org.typelevel.ci.CIString

import java.util.UUID
import scala.util.Try

/** HTTP middleware responsible for bearer-token authentication.
  *
  * Besides rejecting unauthenticated requests, it also propagates the verified user id to
  * downstream routes once the token has already been processed. Keeping both concerns here is
  * intentional: introducing a separate propagation middleware would be artificial because the
  * authenticated subject is only known after this middleware verifies the access token.
  */
object AuthMiddleware {

  private val AuthenticatedUserIdHeader: CIString = CIString("X-Authenticated-User-Id")

  /** Protects routes with bearer-token authentication and enriches successful requests with the
    * authenticated user id for downstream consumers such as GraphQL.
    *
    * @param authService
    *   Authentication service used to verify bearer tokens.
    * @param routes
    *   Routes that should only be reachable for authenticated users.
    * @return
    *   Routes that reject missing/invalid tokens and otherwise forward the request together with
    *   the authenticated user id.
    */
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

            case Some(userId) =>
              routes(attachAuthenticatedUserId(req, userId)).getOrElseF(NotFound())
          }
      }
    }
  }

  /** Reads the authenticated user id previously attached by this middleware.
    *
    * This is an internal transport detail used by downstream HTTP handlers after authentication has
    * already succeeded; it is not intended as a public client-facing header contract.
    *
    * @param request
    *   Request that has already passed through this middleware.
    * @return
    *   The authenticated user id when present and parseable, otherwise `None`.
    */
  def extractAuthenticatedUserId[F[_]](request: Request[F]): Option[UserId] =
    request.headers.headers
      .collectFirst { case header if header.name == AuthenticatedUserIdHeader => header.value }
      .flatMap(value => Try(UUID.fromString(value)).toOption)
      .map(UserId(_))

  private def extractAccessToken[F[_]](request: Request[F]): Option[AccessToken] =
    request.headers.get[Authorization].collect {
      case Authorization(Credentials.Token(AuthScheme.Bearer, token)) =>
        AccessToken(token)
    }

  private def attachAuthenticatedUserId[F[_]](request: Request[F], userId: UserId): Request[F] =
    request.putHeaders(Header.Raw(AuthenticatedUserIdHeader, userId.value.toString))

  private def authenticate[F[_]](
      authService: AuthService[F],
      accessToken: AccessToken
  ): F[Option[UserId]] =
    authService.verifyToken(accessToken)
}
