package io.github.oleksiybondar.api

import cats.effect.{IO, IOApp}
import cats.effect.std.Dispatcher
import io.github.oleksiybondar.api.config.ConfigLoader
import zio.Runtime

import scala.concurrent.ExecutionContext

object Main extends IOApp.Simple {

  given ExecutionContext = ExecutionContext.global
  given Runtime[Any] = Runtime.default

  override def run: IO[Unit] =
    IO.fromEither(ConfigLoader.load()).flatMap { config =>
      Dispatcher.parallel[IO].use { implicit dispatcher =>
        AppServer.resource(config).useForever
      }
    }
}
