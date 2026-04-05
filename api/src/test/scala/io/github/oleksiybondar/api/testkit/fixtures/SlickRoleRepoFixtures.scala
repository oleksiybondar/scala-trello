package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.infrastructure.db.permission.SlickRoleRepo
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object SlickRoleRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private val databaseResource: Resource[IO, Database] =
    TestDatabaseSupport.databaseResource

  private val repoResource: Resource[IO, SlickRoleRepo[IO]] =
    databaseResource.map(new SlickRoleRepo[IO](_))

  def withRepo[A](run: SlickRoleRepo[IO] => IO[A]): A =
    repoResource.use(run).unsafeRunSync()
}
