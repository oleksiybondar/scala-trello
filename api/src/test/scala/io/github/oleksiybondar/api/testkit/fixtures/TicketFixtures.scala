package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.board.BoardId
import io.github.oleksiybondar.api.domain.ticket.{
  Ticket,
  TicketAcceptanceCriteria,
  TicketComponent,
  TicketDescription,
  TicketId,
  TicketName,
  TicketPriority,
  TicketScope
}
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant

object TicketFixtures {

  val sampleTicket: Ticket =
    Ticket(
      id = TicketId(1),
      boardId = BoardFixtures.sampleDashboard.id,
      name = TicketName("Implement login mutation"),
      description = Some(TicketDescription("Add GraphQL mutation for login flow")),
      component = Some(TicketComponent("auth")),
      scope = Some(TicketScope("backend")),
      acceptanceCriteria = Some(TicketAcceptanceCriteria("User can login with valid credentials")),
      createdByUserId = UserId(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")),
      assignedToUserId = Some(
        UserId(java.util.UUID.fromString("22222222-2222-2222-2222-222222222222"))
      ),
      lastModifiedByUserId =
        UserId(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")),
      createdAt = Instant.parse("2026-04-05T09:00:00Z"),
      modifiedAt = Instant.parse("2026-04-05T09:15:00Z"),
      originalEstimatedMinutes = Some(120),
      priority = Some(TicketPriority("high")),
      severityId = Some(TicketSeverityFixtures.normalSeverity.id),
      stateId = TicketStateFixtures.newState.id,
      commentsEnabled = true
    )

  def ticket(
      id: TicketId = sampleTicket.id,
      boardId: BoardId = sampleTicket.boardId,
      name: TicketName = sampleTicket.name,
      description: Option[TicketDescription] = sampleTicket.description,
      component: Option[TicketComponent] = sampleTicket.component,
      scope: Option[TicketScope] = sampleTicket.scope,
      acceptanceCriteria: Option[TicketAcceptanceCriteria] = sampleTicket.acceptanceCriteria,
      createdByUserId: UserId = sampleTicket.createdByUserId,
      assignedToUserId: Option[UserId] = sampleTicket.assignedToUserId,
      lastModifiedByUserId: UserId = sampleTicket.lastModifiedByUserId,
      createdAt: Instant = sampleTicket.createdAt,
      modifiedAt: Instant = sampleTicket.modifiedAt,
      originalEstimatedMinutes: Option[Int] = sampleTicket.originalEstimatedMinutes,
      priority: Option[TicketPriority] = sampleTicket.priority,
      severityId: Option[io.github.oleksiybondar.api.domain.ticket.TicketSeverityId] =
        sampleTicket.severityId,
      stateId: io.github.oleksiybondar.api.domain.ticket.TicketStateId = sampleTicket.stateId,
      commentsEnabled: Boolean = sampleTicket.commentsEnabled
  ): Ticket =
    Ticket(
      id = id,
      boardId = boardId,
      name = name,
      description = description,
      component = component,
      scope = scope,
      acceptanceCriteria = acceptanceCriteria,
      createdByUserId = createdByUserId,
      assignedToUserId = assignedToUserId,
      lastModifiedByUserId = lastModifiedByUserId,
      createdAt = createdAt,
      modifiedAt = modifiedAt,
      originalEstimatedMinutes = originalEstimatedMinutes,
      priority = priority,
      severityId = severityId,
      stateId = stateId,
      commentsEnabled = commentsEnabled
    )
}
