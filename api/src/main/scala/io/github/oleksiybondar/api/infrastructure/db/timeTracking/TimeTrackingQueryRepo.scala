package io.github.oleksiybondar.api.infrastructure.db.timeTracking

import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.timeTracking.{TimeTrackingActivityId, TimeTrackingEntryId}

final case class TimeTrackingQueryRow(
    id: TimeTrackingEntryId,
    ticketId: TicketId,
    userId: String,
    activityId: TimeTrackingActivityId,
    durationMinutes: Int,
    loggedAt: String,
    description: Option[String],
    username: Option[String],
    email: Option[String],
    firstName: String,
    lastName: String,
    avatarUrl: Option[String],
    userCreatedAt: String,
    ticketTitle: String,
    ticketDescription: Option[String]
)

trait TimeTrackingQueryRepo[F[_]] {
  def listByTicket(ticketId: TicketId): F[List[TimeTrackingQueryRow]]
}
