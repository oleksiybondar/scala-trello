package io.github.oleksiybondar.api.http.routes.rest.health

import cats.effect.Async
import org.http4s.HttpRoutes
import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter

object HealthRoutes {

  val healthEndpoint: PublicEndpoint[Unit, Unit, String, Any] =
    endpoint.get
      .in("health")
      .out(stringBody)
      .name("health")
      .description("Health check endpoint")
      .tag("health")

  val all =
    List(healthEndpoint)

  def routes[F[_]: Async]: HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(
      healthServerEndpoint[F]
    )

  private def healthServerEndpoint[F[_]: Async] =
    healthEndpoint.serverLogicSuccess[F](_ => Async[F].pure("ok"))
}