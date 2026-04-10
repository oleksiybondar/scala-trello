package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.config.ConfigLoader
import io.github.oleksiybondar.api.infrastructure.db.DatabaseResource
import io.github.oleksiybondar.api.infrastructure.db.ticket.SlickTicketQueryRepo
import org.flywaydb.core.Flyway
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object SlickTicketQueryRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private val databaseResource: Resource[IO, Database] =
    Resource.eval(IO.fromEither(ConfigLoader.load())).flatMap { config =>
      Resource.eval(resetDatabase(config)).flatMap(_ => DatabaseResource.make(config.database))
    }

  private val repoResource: Resource[IO, (Database, SlickTicketQueryRepo[IO])] =
    databaseResource.map(db => (db, new SlickTicketQueryRepo[IO](db)))

  def withCleanRepo[A](run: SlickTicketQueryRepo[IO] => IO[A]): A =
    repoResource
      .use { case (db, repo) =>
        clearDatabase(db) *> seedBaseData(db) *> run(repo).guarantee(clearDatabase(db))
      }
      .unsafeRunSync()

  def withCleanRepoAndDatabase[A](run: (Database, SlickTicketQueryRepo[IO]) => IO[A]): A =
    repoResource
      .use { case (db, repo) =>
        clearDatabase(db) *> seedBaseData(db) *> run(db, repo).guarantee(clearDatabase(db))
      }
      .unsafeRunSync()

  private def clearDatabase(db: Database): IO[Unit] =
    IO.fromFuture(
      IO(
        db.run(
          sqlu"TRUNCATE TABLE time_tracking, comments, tickets, board_members, boards, permissions, roles, activities, severities, states, auth_sessions, password_history, users CASCADE"
        )
      )
    ).void

  private def seedBaseData(db: Database): IO[Unit] =
    IO.fromFuture(
      IO(
        db.run(
          DBIO.seq(
            sqlu"""
              INSERT INTO users (id, username, email, password_hash, first_name, last_name, avatar_url, created_at)
              VALUES
                ('11111111-1111-1111-1111-111111111111', 'alice', 'alice@example.com', 'hash:secret', 'Alice', 'Example', 'https://example.com/alice.png', TIMESTAMPTZ '2026-04-05T08:00:00Z'),
                ('22222222-2222-2222-2222-222222222222', 'bob', 'bob@example.com', 'hash:secret', 'Bob', 'Example', 'https://example.com/bob.png', TIMESTAMPTZ '2026-04-05T08:30:00Z')
            """,
            sqlu"""
              INSERT INTO boards (id, name, description, active, owner_user_id, created_by_user_id, created_at, modified_at, last_modified_by_user_id)
              VALUES
                ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Core Board', 'Main project dashboard', TRUE, '11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', TIMESTAMPTZ '2026-04-05T08:00:00Z', TIMESTAMPTZ '2026-04-05T08:00:00Z', '11111111-1111-1111-1111-111111111111')
            """,
            sqlu"""
              INSERT INTO states (id, name, description)
              VALUES
                (1, 'new', 'Ticket has been created and is awaiting work.'),
                (2, 'in_progress', 'Work on the ticket is currently in progress.')
            """,
            sqlu"""
              INSERT INTO severities (id, name, description)
              VALUES
                (1, 'minor', 'Low impact issue or task.'),
                (2, 'normal', 'Standard impact issue or task.')
            """,
            sqlu"""
              INSERT INTO activities (id, code, name, description)
              VALUES
                (1, 'code_review', 'Code Review', 'Reviewing implementation changes.'),
                (2, 'development', 'Development', 'Implementing product or technical changes.')
            """,
            sqlu"""
              INSERT INTO tickets (id, dashboard_id, name, description, component, scope, acceptance_criteria, created_by_user_id, assigned_to_user_id, last_modified_by_user_id, created_at, modified_at, original_estimated_minutes, priority, severity_id, state_id, comments_enabled)
              VALUES
                (1, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Implement login mutation', 'Add GraphQL mutation for login flow', 'auth', 'backend', 'User can login with valid credentials', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', TIMESTAMPTZ '2026-04-05T09:00:00Z', TIMESTAMPTZ '2026-04-05T09:15:00Z', 120, 'high', 2, 1, TRUE)
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
