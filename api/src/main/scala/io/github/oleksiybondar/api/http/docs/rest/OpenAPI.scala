package io.github.oleksiybondar.api.http.docs.rest

import cats.effect.Async
import io.github.oleksiybondar.api.http.TapirSupport
import org.http4s.HttpRoutes
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object OpenAPI {

  def routes[F[_]: Async]: HttpRoutes[F] =
    TapirSupport.interpreter[F].toRoutes(
      SwaggerInterpreter()
        .fromEndpoints[F](ApiSpec.all, "API docs", "0.1.0")
    )
}
