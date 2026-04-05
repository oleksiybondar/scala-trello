package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.infrastructure.db.permission.SlickPermissionRepo
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object SlickPermissionRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private val databaseResource: Resource[IO, Database] =
    TestDatabaseSupport.databaseResource

  private val repoResource: Resource[IO, SlickPermissionRepo[IO]] =
    databaseResource.map(new SlickPermissionRepo[IO](_))

  def withRepo[A](run: SlickPermissionRepo[IO] => IO[A]): A =
    repoResource.use(run).unsafeRunSync()
}
