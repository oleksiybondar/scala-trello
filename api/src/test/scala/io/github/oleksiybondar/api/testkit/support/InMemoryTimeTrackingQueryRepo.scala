package io.github.oleksiybondar.api.testkit.support

import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.timeTracking.{TimeTrackingActivityId, TimeTrackingEntryId}
import io.github.oleksiybondar.api.infrastructure.db.timeTracking.{
  TimeTrackingQueryRepo,
  TimeTrackingQueryRow
}

final class InMemoryTimeTrackingQueryRepo[F[_]: Sync](
    entries: List[io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingEntry],
    tickets: List[io.github.oleksiybondar.api.domain.ticket.Ticket],
    users: List[io.github.oleksiybondar.api.domain.user.User]
) extends TimeTrackingQueryRepo[F] {

  override def listByTicket(ticketId: TicketId): F[List[TimeTrackingQueryRow]] =
    entries
      .filter(_.ticketId == ticketId)
      .sortBy(_.loggedAt)(Ordering[java.time.Instant].reverse)
      .flatMap { entry =>
        for {
          ticket <- tickets.find(_.id == entry.ticketId)
          user   <- users.find(_.id == entry.userId)
        } yield TimeTrackingQueryRow(
          id = TimeTrackingEntryId(entry.id.value),
          ticketId = entry.ticketId,
          userId = entry.userId.value.toString,
          activityId = TimeTrackingActivityId(entry.activityId.value),
          durationMinutes = entry.durationMinutes.value,
          loggedAt = entry.loggedAt.toString,
          description = entry.description.map(_.value),
          username = user.username.map(_.value),
          email = user.email.map(_.value),
          firstName = user.firstName.value,
          lastName = user.lastName.value,
          avatarUrl = user.avatarUrl.map(_.value),
          userCreatedAt = user.createdAt.toString,
          ticketTitle = ticket.name.value,
          ticketDescription = ticket.description.map(_.value)
        )
      }
      .pure[F]
}
