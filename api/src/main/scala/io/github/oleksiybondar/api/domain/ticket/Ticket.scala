package io.github.oleksiybondar.api.domain.ticket

import io.github.oleksiybondar.api.domain.board.BoardId
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant

final case class TicketId(value: Long)                   extends AnyVal
final case class TicketName(value: String)               extends AnyVal
final case class TicketDescription(value: String)        extends AnyVal
final case class TicketComponent(value: String)          extends AnyVal
final case class TicketScope(value: String)              extends AnyVal
final case class TicketAcceptanceCriteria(value: String) extends AnyVal
final case class TicketPriority(value: String)           extends AnyVal

final case class Ticket(
    id: TicketId,
    boardId: BoardId,
    name: TicketName,
    description: Option[TicketDescription],
    component: Option[TicketComponent],
    scope: Option[TicketScope],
    acceptanceCriteria: Option[TicketAcceptanceCriteria],
    createdByUserId: UserId,
    assignedToUserId: Option[UserId],
    lastModifiedByUserId: UserId,
    createdAt: Instant,
    modifiedAt: Instant,
    originalEstimatedMinutes: Option[Int],
    priority: Option[TicketPriority],
    severityId: Option[TicketSeverityId],
    stateId: TicketStateId,
    commentsEnabled: Boolean
)
