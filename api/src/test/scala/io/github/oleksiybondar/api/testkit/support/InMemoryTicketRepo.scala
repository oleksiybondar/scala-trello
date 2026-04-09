package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.BoardId
import io.github.oleksiybondar.api.domain.ticket.{Ticket, TicketId}
import io.github.oleksiybondar.api.infrastructure.db.ticket.TicketRepo

final class InMemoryTicketRepo[F[_]: Sync] private (
    state: Ref[F, Map[TicketId, Ticket]]
) extends TicketRepo[F] {

  override def create(ticket: Ticket): F[Unit] =
    state.update(_.updated(ticket.id, ticket))

  override def findById(id: TicketId): F[Option[Ticket]] =
    state.get.map(_.get(id))

  override def listByBoard(boardId: BoardId): F[List[Ticket]] =
    state.get.map(
      _.values.toList
        .filter(_.boardId == boardId)
        .sortBy(_.createdAt)(Ordering[java.time.Instant].reverse)
    )

  override def update(ticket: Ticket): F[Boolean] =
    state.modify { current =>
      if (current.contains(ticket.id)) (current.updated(ticket.id, ticket), true)
      else (current, false)
    }

  override def delete(id: TicketId): F[Boolean] =
    state.modify { current =>
      if (current.contains(id)) (current - id, true)
      else (current, false)
    }
}

object InMemoryTicketRepo {

  def create[F[_]: Sync](tickets: List[Ticket] = Nil): F[InMemoryTicketRepo[F]] =
    Ref.of[F, Map[TicketId, Ticket]](tickets.map(ticket => ticket.id -> ticket).toMap)
      .map(new InMemoryTicketRepo[F](_))
}
