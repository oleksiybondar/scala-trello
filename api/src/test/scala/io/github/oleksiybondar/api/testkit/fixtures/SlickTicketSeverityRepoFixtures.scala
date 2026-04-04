package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.config.ConfigLoader
import io.github.oleksiybondar.api.infrastructure.db.DatabaseResource
import io.github.oleksiybondar.api.infrastructure.db.ticket.SlickTicketSeverityRepo
import org.flywaydb.core.Flyway
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object SlickTicketSeverityRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private val databaseResource: Resource[IO, Database] =
    Resource.eval(IO.fromEither(ConfigLoader.load())).flatMap { config =>
      Resource.eval(resetDatabase(config)).flatMap { _ =>
        DatabaseResource.make(config.database)
      }
    }

  private val repoResource: Resource[IO, SlickTicketSeverityRepo[IO]] =
    databaseResource.map(new SlickTicketSeverityRepo[IO](_))

  def withRepo[A](run: SlickTicketSeverityRepo[IO] => IO[A]): A =
    repoResource.use(run).unsafeRunSync()

  private def resetDatabase(config: io.github.oleksiybondar.api.config.AppConfig): IO[Unit] =
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
