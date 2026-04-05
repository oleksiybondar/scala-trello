package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import io.github.oleksiybondar.api.infrastructure.db.ticket.SlickTicketStateRepo
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object SlickTicketStateRepoFixtures {

  given ExecutionContext = ExecutionContext.global

  private val databaseResource: Resource[IO, Database] =
    TestDatabaseSupport.databaseResource

  private val repoResource: Resource[IO, SlickTicketStateRepo[IO]] =
    databaseResource.map(new SlickTicketStateRepo[IO](_))

  def withRepo[A](run: SlickTicketStateRepo[IO] => IO[A]): A =
    repoResource.use(run).unsafeRunSync()
}
