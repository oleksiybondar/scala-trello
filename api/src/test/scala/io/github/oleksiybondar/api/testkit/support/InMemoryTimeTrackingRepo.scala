package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.timeTracking.{TimeTrackingEntry, TimeTrackingEntryId}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.timeTracking.TimeTrackingRepo

final class InMemoryTimeTrackingRepo[F[_]: Sync] private (
    state: Ref[F, Map[TimeTrackingEntryId, TimeTrackingEntry]]
) extends TimeTrackingRepo[F] {

  override def nextId: F[TimeTrackingEntryId] =
    state.get.map(current =>
      TimeTrackingEntryId(current.keys.map(_.value).maxOption.getOrElse(0L) + 1L)
    )

  override def create(entry: TimeTrackingEntry): F[Unit] =
    state.update(_.updated(entry.id, entry))

  override def findById(id: TimeTrackingEntryId): F[Option[TimeTrackingEntry]] =
    state.get.map(_.get(id))

  override def listByTicket(ticketId: TicketId): F[List[TimeTrackingEntry]] =
    state.get.map(
      _.values.toList
        .filter(_.ticketId == ticketId)
        .sortBy(_.loggedAt)(Ordering[java.time.Instant].reverse)
    )

  override def listByUser(userId: UserId): F[List[TimeTrackingEntry]] =
    state.get.map(
      _.values.toList
        .filter(_.userId == userId)
        .sortBy(_.loggedAt)(Ordering[java.time.Instant].reverse)
    )

  override def update(entry: TimeTrackingEntry): F[Boolean] =
    state.modify { current =>
      if (current.contains(entry.id)) (current.updated(entry.id, entry), true)
      else (current, false)
    }

  override def delete(id: TimeTrackingEntryId): F[Boolean] =
    state.modify { current =>
      if (current.contains(id)) (current - id, true)
      else (current, false)
    }
}

object InMemoryTimeTrackingRepo {

  def create[F[_]: Sync](entries: List[TimeTrackingEntry] = Nil): F[InMemoryTimeTrackingRepo[F]] =
    Ref.of[F, Map[TimeTrackingEntryId, TimeTrackingEntry]](entries.map(entry =>
      entry.id -> entry
    ).toMap)
      .map(new InMemoryTimeTrackingRepo[F](_))
}
