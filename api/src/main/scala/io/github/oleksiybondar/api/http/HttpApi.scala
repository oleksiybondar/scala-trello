package io.github.oleksiybondar.api.http

import cats.Monad
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{HttpApp, HttpRoutes}

object HttpApi {

  def make[F[_]: Monad](
      healthRoutes: HttpRoutes[F],
      authRoutes: HttpRoutes[F],
      graphqlRoutes: HttpRoutes[F],
      swaggerRoutes: HttpRoutes[F],
      graphiqlRoutes: HttpRoutes[F]
  ): HttpApp[F] =
    Router(
      "/"        -> healthRoutes,
      "/"        -> authRoutes,
      "/graphql" -> graphqlRoutes,
      "/"        -> swaggerRoutes,
      "/"        -> graphiqlRoutes
    ).orNotFound
}
