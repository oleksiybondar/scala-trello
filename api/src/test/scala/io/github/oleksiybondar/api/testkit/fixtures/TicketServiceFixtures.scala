package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.board.{Board, BoardMember}
import io.github.oleksiybondar.api.domain.permission.{Permission, Role}
import io.github.oleksiybondar.api.domain.ticket.{Ticket, TicketServiceLive}
import io.github.oleksiybondar.api.testkit.support.InMemoryTicketRepo

object TicketServiceFixtures {

  final case class TicketServiceContext(
      ticketRepo: InMemoryTicketRepo[IO],
      boardAccessFixtures: BoardAccessServiceFixtures.BoardAccessServiceContext,
      ticketService: TicketServiceLive[IO]
  )

  def withTicketService[A](
      tickets: List[Ticket] = Nil,
      dashboards: List[Board] = Nil,
      members: List[BoardMember] = Nil,
      roles: List[Role] = Nil,
      permissions: List[Permission] = Nil
  )(run: TicketServiceContext => IO[A]): A =
    (
      for {
        ticketRepo <- InMemoryTicketRepo.create[IO](tickets)
        result     <- IO.pure(
                        BoardAccessServiceFixtures.withBoardAccessService(
                          dashboards = dashboards,
                          members = members,
                          roles = roles,
                          permissions = permissions
                        ) { accessCtx =>
                          run(
                            TicketServiceContext(
                              ticketRepo = ticketRepo,
                              boardAccessFixtures = accessCtx,
                              ticketService = new TicketServiceLive[IO](
                                ticketRepo,
                                accessCtx.dashboardAccessService,
                                accessCtx.membershipFixtures.dashboardMembershipService
                              )
                            )
                          )
                        }
                      )
      } yield result
    ).unsafeRunSync()
}
