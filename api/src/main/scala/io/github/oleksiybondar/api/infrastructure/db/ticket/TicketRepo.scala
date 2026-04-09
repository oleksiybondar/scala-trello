package io.github.oleksiybondar.api.infrastructure.db.ticket

import io.github.oleksiybondar.api.domain.board.BoardId
import io.github.oleksiybondar.api.domain.ticket.{Ticket, TicketId}

trait TicketRepo[F[_]] {
  def create(ticket: Ticket): F[Unit]
  def findById(id: TicketId): F[Option[Ticket]]
  def listByBoard(boardId: BoardId): F[List[Ticket]]
  def update(ticket: Ticket): F[Boolean]
  def delete(id: TicketId): F[Boolean]
}
