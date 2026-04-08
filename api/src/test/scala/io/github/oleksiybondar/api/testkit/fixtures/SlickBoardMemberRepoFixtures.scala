package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.config.ConfigLoader
import io.github.oleksiybondar.api.infrastructure.db.DatabaseResource
import io.github.oleksiybondar.api.infrastructure.db.board.SlickBoardMemberRepo
import org.flywaydb.core.Flyway
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object SlickBoardMemberRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private val databaseResource: Resource[IO, Database] =
    Resource.eval(IO.fromEither(ConfigLoader.load())).flatMap { config =>
      Resource.eval(resetDatabase(config)).flatMap(_ => DatabaseResource.make(config.database))
    }

  private val repoResource: Resource[IO, (Database, SlickBoardMemberRepo[IO])] =
    databaseResource.map(db => (db, new SlickBoardMemberRepo[IO](db)))

  def withCleanRepo[A](run: SlickBoardMemberRepo[IO] => IO[A]): A =
    repoResource
      .use { case (db, repo) =>
        clearDatabase(db) *> seedDependencies(db) *> run(repo).guarantee(clearDatabase(db))
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

  private def seedDependencies(db: Database): IO[Unit] =
    IO.fromFuture(
      IO(
        db.run(
          DBIO.seq(
            sqlu"""
              INSERT INTO users (id, username, email, password_hash, first_name, last_name, avatar_url, created_at)
              VALUES
                ('11111111-1111-1111-1111-111111111111', 'alice', 'alice@example.com', 'hash:secret', 'Alice', 'Example', NULL, TIMESTAMPTZ '2026-04-06T08:00:00Z'),
                ('22222222-2222-2222-2222-222222222222', 'bob', 'bob@example.com', 'hash:secret', 'Bob', 'Example', NULL, TIMESTAMPTZ '2026-04-06T08:10:00Z')
            """,
            sqlu"""
              INSERT INTO roles (id, name, description)
              VALUES
                (1, 'admin', 'Full dashboard access including member management.'),
                (2, 'contributor', 'Can contribute to tickets and comments.'),
                (3, 'viewer', 'Read-only access to dashboard data.')
            """,
            sqlu"ALTER TABLE roles ALTER COLUMN id RESTART WITH 4",
            sqlu"""
              INSERT INTO boards (id, name, description, active, owner_user_id, created_by_user_id, created_at, modified_at, last_modified_by_user_id)
              VALUES
                ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Core Board', 'Main project dashboard', TRUE, '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', TIMESTAMPTZ '2026-04-06T08:00:00Z', TIMESTAMPTZ '2026-04-06T08:00:00Z', '11111111-1111-1111-1111-111111111111'),
                ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Secondary Board', 'Second project dashboard', TRUE, '22222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', TIMESTAMPTZ '2026-04-06T09:00:00Z', TIMESTAMPTZ '2026-04-06T09:00:00Z', '22222222-2222-2222-2222-222222222222')
            """
          )
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
