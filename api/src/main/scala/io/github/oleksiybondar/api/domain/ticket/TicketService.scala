package io.github.oleksiybondar.api.domain.ticket

import io.github.oleksiybondar.api.domain.board.BoardId
import io.github.oleksiybondar.api.domain.user.UserId

final case class CreateTicketCommand(
    boardId: BoardId,
    name: TicketName,
    description: Option[TicketDescription] = None,
    component: Option[TicketComponent] = None,
    scope: Option[TicketScope] = None,
    acceptanceCriteria: Option[TicketAcceptanceCriteria] = None,
    assignedToUserId: Option[UserId] = None,
    originalEstimatedMinutes: Option[Int] = None,
    priority: Option[TicketPriority] = None,
    severityId: Option[TicketSeverityId] = None,
    stateId: TicketStateId
)

trait TicketService[F[_]] {
  def createTicket(command: CreateTicketCommand, actorUserId: UserId): F[Option[Ticket]]
  def getTicket(id: TicketId): F[Option[Ticket]]
  def listTickets(boardId: BoardId, actorUserId: UserId): F[List[Ticket]]
  def changeTitle(ticketId: TicketId, actorUserId: UserId, title: TicketName): F[Boolean]
  def changeDescription(
      ticketId: TicketId,
      actorUserId: UserId,
      description: Option[TicketDescription]
  ): F[Boolean]
  def changeAcceptanceCriteria(
      ticketId: TicketId,
      actorUserId: UserId,
      acceptanceCriteria: Option[TicketAcceptanceCriteria]
  ): F[Boolean]
  def changeEstimatedTime(
      ticketId: TicketId,
      actorUserId: UserId,
      estimatedMinutes: Option[Int]
  ): F[Boolean]
  def reassignTicket(
      ticketId: TicketId,
      actorUserId: UserId,
      assignedToUserId: Option[UserId]
  ): F[Boolean]
  def deleteTicket(ticketId: TicketId, actorUserId: UserId): F[Boolean]
}
