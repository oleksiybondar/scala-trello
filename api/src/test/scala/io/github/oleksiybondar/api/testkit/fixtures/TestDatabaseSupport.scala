package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.config.{AppConfig, ConfigLoader}
import io.github.oleksiybondar.api.infrastructure.db.DatabaseResource
import org.flywaydb.core.Flyway
import slick.jdbc.PostgresProfile.api._

object TestDatabaseSupport {

  private val testUserEmailPattern    = "spec-user+%@example.com"
  private val testUsernamePattern     = "spec-user-%"
  private val trackedUserDeleteClause =
    s"""
       |email LIKE '$testUserEmailPattern'
       |OR username LIKE '$testUsernamePattern'
       |""".stripMargin.replaceAll("\n", " ")

  val databaseResource: Resource[IO, Database] =
    Resource.eval(IO.fromEither(ConfigLoader.load())).flatMap { config =>
      Resource.eval(migrateDatabase(config)).flatMap { _ =>
        DatabaseResource.make(config.database)
      }
    }

  def migrateDatabase(config: AppConfig): IO[Unit] =
    IO.blocking {
      val flyway =
        Flyway
          .configure()
          .dataSource(
            config.database.db.url,
            config.database.db.user,
            config.database.db.password
          )
          .load()

      flyway.migrate()
      ()
    }

  def clearTrackedUserData(db: Database): IO[Unit] =
    IO.fromFuture(
      IO(
        db.run(
          sqlu"""
            DELETE FROM users
            WHERE #$trackedUserDeleteClause
          """
        )
      )
    ).void
}
