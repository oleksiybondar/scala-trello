package io.github.oleksiybondar.api

import cats.effect.{IO, IOApp}
import io.github.oleksiybondar.api.config.ConfigLoader
import io.github.oleksiybondar.api.infrastructure.db.MigrationRunner

object MigrateMain extends IOApp.Simple {
  override def run: IO[Unit] =
    for {
      config   <- IO.fromEither(ConfigLoader.load())
      executed <- IO.blocking(MigrationRunner.migrate(config.database))
      _        <- IO.println(s"Flyway migrations executed: $executed")
    } yield ()
}
