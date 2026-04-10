package io.github.oleksiybondar.api.modules

import cats.effect.Async
import io.github.oleksiybondar.api.domain.board.{BoardAccessService, BoardMembershipService}
import io.github.oleksiybondar.api.domain.timeTracking.{
  TimeTrackingService,
  TimeTrackingServiceLive
}
import io.github.oleksiybondar.api.infrastructure.db.board.BoardRepo
import io.github.oleksiybondar.api.infrastructure.db.ticket.TicketRepo
import io.github.oleksiybondar.api.infrastructure.db.timeTracking.{
  SlickTimeTrackingRepo,
  TimeTrackingActivityRepo,
  TimeTrackingRepo
}
import slick.jdbc.PostgresProfile.api.Database

final case class TimeTrackingModule[F[_]](
    timeTrackingRepo: TimeTrackingRepo[F],
    timeTrackingService: TimeTrackingService[F]
)

object TimeTrackingModule {

  def make[F[_]: Async](
      db: Database,
      boardRepo: BoardRepo[F],
      ticketRepo: TicketRepo[F],
      boardAccessService: BoardAccessService[F],
      boardMembershipService: BoardMembershipService[F],
      timeTrackingActivityRepo: TimeTrackingActivityRepo[F]
  ): TimeTrackingModule[F] = {
    val timeTrackingRepo = new SlickTimeTrackingRepo[F](db)

    TimeTrackingModule(
      timeTrackingRepo = timeTrackingRepo,
      timeTrackingService = new TimeTrackingServiceLive[F](
        timeTrackingRepo,
        ticketRepo,
        boardRepo,
        boardAccessService,
        boardMembershipService,
        timeTrackingActivityRepo
      )
    )
  }
}
