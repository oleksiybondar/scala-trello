package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.ticket.{
  TicketSeverity,
  TicketSeverityId,
  TicketSeverityName
}
import io.github.oleksiybondar.api.infrastructure.db.ticket.TicketSeverityRepo

final class InMemoryTicketSeverityRepo[F[_]: Sync] private (
    state: Ref[F, Map[TicketSeverityId, TicketSeverity]]
) extends TicketSeverityRepo[F] {

  override def findById(id: TicketSeverityId): F[Option[TicketSeverity]] =
    state.get.map(_.get(id))

  override def findByName(name: TicketSeverityName): F[Option[TicketSeverity]] =
    state.get.map(_.values.find(_.name == name))

  override def list: F[List[TicketSeverity]] =
    state.get.map(_.values.toList.sortBy(_.id.value))
}

object InMemoryTicketSeverityRepo {

  def create[F[_]: Sync](
      severities: List[TicketSeverity] = Nil
  ): F[InMemoryTicketSeverityRepo[F]] =
    Ref.of[F, Map[TicketSeverityId, TicketSeverity]](
      severities.map(severity => severity.id -> severity).toMap
    ).map(new InMemoryTicketSeverityRepo[F](_))
}
