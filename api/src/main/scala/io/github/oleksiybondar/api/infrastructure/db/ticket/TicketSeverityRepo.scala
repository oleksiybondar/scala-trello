package io.github.oleksiybondar.api.infrastructure.db.ticket

import io.github.oleksiybondar.api.domain.ticket.{
  TicketSeverity,
  TicketSeverityId,
  TicketSeverityName
}

trait TicketSeverityRepo[F[_]] {
  def findById(id: TicketSeverityId): F[Option[TicketSeverity]]
  def findByName(name: TicketSeverityName): F[Option[TicketSeverity]]
  def list: F[List[TicketSeverity]]
}
