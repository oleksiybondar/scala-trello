package io.github.oleksiybondar.api

import cats.effect.{IO, IOApp}
import io.github.oleksiybondar.api.config.ConfigLoader
import io.github.oleksiybondar.api.infrastructure.db.DatabaseResource
import io.github.oleksiybondar.api.infrastructure.db.auth.AuthSessionRepoSlick

import java.time.Instant

object CleanupSessionDataMain extends IOApp.Simple {

  override def run: IO[Unit] =
    for {
      config  <- IO.fromEither(ConfigLoader.load())
      deleted <- databaseResource(config).use(cleanExpiredOrRevokedSessions)
      _       <- IO.println(s"Deleted auth sessions: $deleted")
    } yield ()

  private def databaseResource(config: io.github.oleksiybondar.api.config.AppConfig) =
    DatabaseResource.make(config.database)

  def cleanExpiredOrRevokedSessions(
      db: slick.jdbc.PostgresProfile.api.Database
  ): IO[Int] =
    new AuthSessionRepoSlick[IO](db).deleteExpiredOrRevoked(Instant.now())
}
