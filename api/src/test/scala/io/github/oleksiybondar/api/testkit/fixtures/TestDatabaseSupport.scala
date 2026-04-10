package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.config.{AppConfig, ConfigLoader}
import io.github.oleksiybondar.api.infrastructure.db.DatabaseResource
import org.flywaydb.core.Flyway
import slick.jdbc.PostgresProfile.api._

object TestDatabaseSupport {

  val databaseResource: Resource[IO, Database] =
    Resource.eval(IO.fromEither(ConfigLoader.load())).flatMap { config =>
      Resource.eval(resetDatabase(config)).flatMap { _ =>
        DatabaseResource.make(config.database)
      }
    }

  def clearDatabase(db: Database): IO[Unit] =
    IO.fromFuture(
      IO(
        db.run(
          sqlu"TRUNCATE TABLE time_tracking, comments, tickets, board_members, boards, permissions, roles, activities, severities, states, auth_sessions, password_history, users CASCADE"
        )
      )
    ).void

  def resetDatabase(config: AppConfig): IO[Unit] =
    IO.blocking {
      val flyway =
        Flyway
          .configure()
          .cleanDisabled(false)
          .dataSource(
            config.database.db.url,
            config.database.db.user,
            config.database.db.password
          )
          .load()

      flyway.clean()
      flyway.migrate()
      ()
    }
}
