package io.github.oleksiybondar.api.http.routes.rest.health

import cats.effect.Async
import cats.syntax.all._
import io.circe.Codec
import io.github.oleksiybondar.api.http.TapirSupport
import org.http4s.HttpRoutes
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object HealthRoutes {

  final case class HealthResponse(
      status: String,
      service: String,
      version: String,
      timestamp: String
  )

  given Codec.AsObject[HealthResponse] = io.circe.generic.semiauto.deriveCodec

  private val ServiceName = "api"
  private val Version     = "0.1.0-SNAPSHOT"

  val healthEndpoint: PublicEndpoint[Unit, Unit, HealthResponse, Any] =
    endpoint.get
      .in("health")
      .out(jsonBody[HealthResponse])
      .name("health")
      .description("Health check endpoint")
      .tag("health")

  val all: List[PublicEndpoint[Unit, Unit, HealthResponse, Any]] =
    List(healthEndpoint)

  def routes[F[_]: Async]: HttpRoutes[F] =
    TapirSupport.interpreter[F].toRoutes(
      healthServerEndpoint[F]
    )

  private def healthServerEndpoint[F[_]: Async] =
    healthEndpoint.serverLogicSuccess[F](_ =>
      Async[F].realTimeInstant.map(now =>
        HealthResponse(
          status = "ok",
          service = ServiceName,
          version = Version,
          timestamp = now.toString
        )
      )
    )
}
