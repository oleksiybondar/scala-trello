package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.ticket.{TicketState, TicketStateId, TicketStateName}
import io.github.oleksiybondar.api.infrastructure.db.ticket.TicketStateRepo

final class InMemoryTicketStateRepo[F[_]: Sync] private (
    state: Ref[F, Map[TicketStateId, TicketState]]
) extends TicketStateRepo[F] {

  override def findById(id: TicketStateId): F[Option[TicketState]] =
    state.get.map(_.get(id))

  override def findByName(name: TicketStateName): F[Option[TicketState]] =
    state.get.map(_.values.find(_.name == name))

  override def list: F[List[TicketState]] =
    state.get.map(_.values.toList.sortBy(_.id.value))
}

object InMemoryTicketStateRepo {

  def create[F[_]: Sync](states: List[TicketState] = Nil): F[InMemoryTicketStateRepo[F]] =
    Ref.of[F, Map[TicketStateId, TicketState]](states.map(state => state.id -> state).toMap)
      .map(new InMemoryTicketStateRepo[F](_))
}
