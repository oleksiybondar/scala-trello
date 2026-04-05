package io.github.oleksiybondar.api.infrastructure.db.ticket

import io.github.oleksiybondar.api.domain.ticket.{TicketState, TicketStateId, TicketStateName}

trait TicketStateRepo[F[_]] {
  def findById(id: TicketStateId): F[Option[TicketState]]
  def findByName(name: TicketStateName): F[Option[TicketState]]
  def list: F[List[TicketState]]
}
