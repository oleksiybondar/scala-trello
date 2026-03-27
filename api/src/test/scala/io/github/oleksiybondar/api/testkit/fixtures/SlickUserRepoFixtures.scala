package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.config.ConfigLoader
import io.github.oleksiybondar.api.infrastructure.db.{DatabaseResource, MigrationRunner}
import io.github.oleksiybondar.api.infrastructure.db.user.SlickUserRepo
import slick.jdbc.PostgresProfile.api.*

import scala.concurrent.ExecutionContext

object SlickUserRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private lazy val config =
    ConfigLoader.load().fold(throw _, identity)

  private val databaseResource: Resource[IO, Database] =
    Resource.eval(IO.blocking(MigrationRunner.migrate(config.database))).flatMap { _ =>
      DatabaseResource.make(config.database)
    }

  private val repoResource: Resource[IO, (Database, SlickUserRepo[IO])] =
    databaseResource.map { db =>
      (db, new SlickUserRepo[IO](db))
    }

  def withCleanRepo[A](run: SlickUserRepo[IO] => IO[A]): A =
    repoResource
      .use { case (db, repo) =>
        for {
          _ <- clearDatabase(db)
          result <- run(repo)
        } yield result
      }
      .unsafeRunSync()

  def withCleanDatabase[A](run: Database => IO[A]): A =
    databaseResource
      .use { db =>
        for {
          _ <- clearDatabase(db)
          result <- run(db)
        } yield result
      }
      .unsafeRunSync()

  private def clearDatabase(db: Database): IO[Unit] =
    IO.fromFuture(IO(db.run(sqlu"TRUNCATE TABLE auth_tokens, users CASCADE"))).void
}
