package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.infrastructure.db.timeTracking.SlickTimeTrackingActivityRepo
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object SlickTimeTrackingActivityRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private val databaseResource: Resource[IO, Database] =
    TestDatabaseSupport.databaseResource

  private val repoResource: Resource[IO, SlickTimeTrackingActivityRepo[IO]] =
    databaseResource.map(new SlickTimeTrackingActivityRepo[IO](_))

  def withRepo[A](run: SlickTimeTrackingActivityRepo[IO] => IO[A]): A =
    repoResource.use(run).unsafeRunSync()
}
