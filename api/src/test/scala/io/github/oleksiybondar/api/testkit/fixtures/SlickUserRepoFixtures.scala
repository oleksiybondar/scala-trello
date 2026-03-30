package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.config.ConfigLoader
import io.github.oleksiybondar.api.infrastructure.db.DatabaseResource
import io.github.oleksiybondar.api.infrastructure.db.user.SlickUserRepo
import org.flywaydb.core.Flyway
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object SlickUserRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private val databaseResource: Resource[IO, Database] =
    Resource.eval(IO.fromEither(ConfigLoader.load())).flatMap { config =>
      Resource.eval(resetDatabase(config)).flatMap { _ =>
        DatabaseResource.make(config.database)
      }
    }

  private val repoResource: Resource[IO, (Database, SlickUserRepo[IO])] =
    databaseResource.map { db =>
      (db, new SlickUserRepo[IO](db))
    }

  def withCleanRepo[A](run: SlickUserRepo[IO] => IO[A]): A =
    repoResource
      .use { case (db, repo) =>
        clearDatabase(db) *> run(repo).guarantee(clearDatabase(db))
      }
      .unsafeRunSync()

  def withCleanDatabase[A](run: Database => IO[A]): A =
    databaseResource
      .use { db =>
        clearDatabase(db) *> run(db).guarantee(clearDatabase(db))
      }
      .unsafeRunSync()

  private def clearDatabase(db: Database): IO[Unit] =
    IO.fromFuture(IO(db.run(sqlu"TRUNCATE TABLE auth_sessions, users CASCADE"))).void

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
