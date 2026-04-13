package io.github.oleksiybondar.api.modules

import cats.effect.Async
import io.github.oleksiybondar.api.domain.board.{BoardAccessService, BoardMembershipService}
import io.github.oleksiybondar.api.domain.ticket.{TicketService, TicketServiceLive}
import io.github.oleksiybondar.api.infrastructure.db.board.BoardRepo
import io.github.oleksiybondar.api.infrastructure.db.ticket.{
  SlickTicketQueryRepo,
  SlickTicketRepo,
  SlickTicketStateRepo,
  TicketQueryRepo,
  TicketRepo
}
import slick.jdbc.PostgresProfile.api.Database

final case class TicketModule[F[_]](
    ticketRepo: TicketRepo[F],
    ticketQueryRepo: TicketQueryRepo[F],
    ticketService: TicketService[F]
)

object TicketModule {

  def make[F[_]: Async](
      db: Database,
      boardRepo: BoardRepo[F],
      boardAccessService: BoardAccessService[F],
      boardMembershipService: BoardMembershipService[F]
  ): TicketModule[F] = {
    val ticketRepo      = new SlickTicketRepo[F](db)
    val ticketQueryRepo = new SlickTicketQueryRepo[F](db)
    val ticketStateRepo = new SlickTicketStateRepo[F](db)

    TicketModule(
      ticketRepo = ticketRepo,
      ticketQueryRepo = ticketQueryRepo,
      ticketService = new TicketServiceLive[F](
        ticketRepo,
        ticketStateRepo,
        boardRepo,
        boardAccessService,
        boardMembershipService
      )
    )
  }
}
