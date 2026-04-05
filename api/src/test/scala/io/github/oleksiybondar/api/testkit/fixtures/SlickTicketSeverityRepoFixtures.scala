package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.infrastructure.db.ticket.SlickTicketSeverityRepo
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object SlickTicketSeverityRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private val databaseResource: Resource[IO, Database] =
    TestDatabaseSupport.databaseResource

  private val repoResource: Resource[IO, SlickTicketSeverityRepo[IO]] =
    databaseResource.map(new SlickTicketSeverityRepo[IO](_))

  def withRepo[A](run: SlickTicketSeverityRepo[IO] => IO[A]): A =
    repoResource.use(run).unsafeRunSync()
}
