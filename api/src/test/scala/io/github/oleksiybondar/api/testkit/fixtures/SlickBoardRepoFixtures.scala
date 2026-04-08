package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.config.ConfigLoader
import io.github.oleksiybondar.api.infrastructure.db.DatabaseResource
import io.github.oleksiybondar.api.infrastructure.db.board.SlickBoardRepo
import org.flywaydb.core.Flyway
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object SlickBoardRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private val databaseResource: Resource[IO, Database] =
    Resource.eval(IO.fromEither(ConfigLoader.load())).flatMap { config =>
      Resource.eval(resetDatabase(config)).flatMap { _ =>
        DatabaseResource.make(config.database)
      }
    }

  private val repoResource: Resource[IO, (Database, SlickBoardRepo[IO])] =
    databaseResource.map { db =>
      (db, new SlickBoardRepo[IO](db))
    }

  def withCleanRepo[A](run: SlickBoardRepo[IO] => IO[A]): A =
    repoResource
      .use { case (db, repo) =>
        clearDatabase(db) *> seedUsers(db) *> run(repo).guarantee(clearDatabase(db))
      }
      .unsafeRunSync()

  def withCleanRepoAndDatabase[A](run: (Database, SlickBoardRepo[IO]) => IO[A]): A =
    repoResource
      .use { case (db, repo) =>
        clearDatabase(db) *> seedUsers(db) *> run(db, repo).guarantee(clearDatabase(db))
      }
      .unsafeRunSync()

  private def clearDatabase(db: Database): IO[Unit] =
    IO.fromFuture(
      IO(
        db.run(
          sqlu"TRUNCATE TABLE time_tracking, comments, tickets, dashboard_members, boards, permissions, roles, activities, severities, states, auth_sessions, password_history, users CASCADE"
        )
      )
    ).void

  private def seedUsers(db: Database): IO[Unit] =
    IO.fromFuture(
      IO(
        db.run(
          sqlu"""
            INSERT INTO users (id, username, email, password_hash, first_name, last_name, avatar_url, created_at)
            VALUES
              ('11111111-1111-1111-1111-111111111111', 'alice', 'alice@example.com', 'hash:secret', 'Alice', 'Example', NULL, TIMESTAMPTZ '2026-04-05T08:00:00Z'),
              ('22222222-2222-2222-2222-222222222222', 'bob', 'bob@example.com', 'hash:secret', 'Bob', 'Example', NULL, TIMESTAMPTZ '2026-04-05T08:30:00Z')
          """
        )
      )
    ).void

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
