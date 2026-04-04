package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.infrastructure.db.user.SlickUserRepo
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object SlickUserRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private val databaseResource: Resource[IO, Database] =
    TestDatabaseSupport.databaseResource

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
    TestDatabaseSupport.clearTrackedUserData(db)
}
