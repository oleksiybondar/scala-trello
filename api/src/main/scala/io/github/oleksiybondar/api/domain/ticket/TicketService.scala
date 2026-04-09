package io.github.oleksiybondar.api.domain.ticket

import io.github.oleksiybondar.api.domain.user.UserId

trait TicketService[F[_]] {
  def createTicket(ticket: Ticket, actorUserId: UserId): F[Boolean]
  def getTicket(id: TicketId): F[Option[Ticket]]
  def modifyTicket(ticket: Ticket, actorUserId: UserId): F[Boolean]
  def reassignTicket(
      ticketId: TicketId,
      actorUserId: UserId,
      assignedToUserId: Option[UserId]
  ): F[Boolean]
  def deleteTicket(ticketId: TicketId, actorUserId: UserId): F[Boolean]
}
