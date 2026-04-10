package io.github.oleksiybondar.api.http

import cats.effect.Async
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}

object TapirSupport {

  def interpreter[F[_]: Async]: Http4sServerInterpreter[F] =
    Http4sServerInterpreter[F](
      Http4sServerOptions
        .customiseInterceptors[F]
        .serverLog(None)
        .options
    )
}
