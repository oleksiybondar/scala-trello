package io.github.oleksiybondar.api.infrastructure.db.ticket

import io.github.oleksiybondar.api.domain.ticket.{TicketId, TicketStateId}
import io.github.oleksiybondar.api.domain.timeTracking.{TimeTrackingActivityId, TimeTrackingEntryId}

final case class TicketQueryUserRow(
    id: String,
    username: Option[String],
    email: Option[String],
    firstName: String,
    lastName: String,
    avatarUrl: Option[String],
    createdAt: String
)

final case class TicketQueryBoardRow(
    id: String,
    name: String,
    active: Boolean
)

final case class TicketQueryTimeEntryRow(
    id: TimeTrackingEntryId,
    ticketId: TicketId,
    userId: String,
    activityId: TimeTrackingActivityId,
    activityCode: String,
    activityName: String,
    durationMinutes: Int,
    loggedAt: String,
    description: Option[String],
    user: TicketQueryUserRow
)

final case class TicketQueryRow(
    id: TicketId,
    boardId: String,
    name: String,
    description: Option[String],
    acceptanceCriteria: Option[String],
    estimatedMinutes: Option[Int],
    createdByUserId: String,
    assignedToUserId: Option[String],
    lastModifiedByUserId: String,
    createdAt: String,
    modifiedAt: String,
    stateId: TicketStateId,
    commentsCount: Int,
    board: TicketQueryBoardRow,
    createdBy: TicketQueryUserRow,
    assignedTo: Option[TicketQueryUserRow],
    lastModifiedBy: TicketQueryUserRow,
    timeEntries: List[TicketQueryTimeEntryRow]
)

trait TicketQueryRepo[F[_]] {
  def findById(ticketId: TicketId): F[Option[TicketQueryRow]]
}
