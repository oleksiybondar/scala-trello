package io.github.oleksiybondar.api.modules

import cats.effect.Async
import io.github.oleksiybondar.api.infrastructure.db.ticket.{
  SlickTicketSeverityRepo,
  SlickTicketStateRepo,
  TicketSeverityRepo,
  TicketStateRepo
}
import io.github.oleksiybondar.api.infrastructure.db.timeTracking.{
  SlickTimeTrackingActivityRepo,
  TimeTrackingActivityRepo
}
import slick.jdbc.PostgresProfile.api.Database

final case class DictionaryModule[F[_]](
    ticketStateRepo: TicketStateRepo[F],
    ticketSeverityRepo: TicketSeverityRepo[F],
    timeTrackingActivityRepo: TimeTrackingActivityRepo[F]
)

object DictionaryModule {

  def make[F[_]: Async](db: Database): DictionaryModule[F] =
    DictionaryModule(
      ticketStateRepo = new SlickTicketStateRepo[F](db),
      ticketSeverityRepo = new SlickTicketSeverityRepo[F](db),
      timeTrackingActivityRepo = new SlickTimeTrackingActivityRepo[F](db)
    )
}
