package io.github.oleksiybondar.api.infrastructure.db

import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.config.DatabaseConfig
import slick.jdbc.PostgresProfile.api.Database

object DatabaseResource {

  def make(config: DatabaseConfig): Resource[IO, Database] =
    Resource.make {
      IO.blocking(
        Database.forURL(
          url = config.db.url,
          user = config.db.user,
          password = config.db.password,
          driver = config.db.driver
        )
      )
    } { db =>
      IO.blocking(db.close()).handleErrorWith(_ => IO.unit)
    }
}
