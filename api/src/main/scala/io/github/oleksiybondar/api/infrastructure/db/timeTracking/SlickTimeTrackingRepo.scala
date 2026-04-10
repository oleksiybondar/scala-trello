package io.github.oleksiybondar.api.infrastructure.db.timeTracking

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.timeTracking.{
  TimeTrackingActivityId,
  TimeTrackingDurationMinutes,
  TimeTrackingEntry,
  TimeTrackingEntryDescription,
  TimeTrackingEntryId
}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.SharedSlickTables.{
  TimeTrackingRow,
  TimeTrackingTable
}
import slick.jdbc.PostgresProfile.api._

final class SlickTimeTrackingRepo[F[_]: Async](db: Database) extends TimeTrackingRepo[F] {

  private val timeTrackingEntries = TableQuery[TimeTrackingTable]

  override def nextId: F[TimeTrackingEntryId] =
    run(timeTrackingEntries.map(_.id).max.result).map(id =>
      TimeTrackingEntryId(id.getOrElse(0L) + 1L)
    )

  override def create(entry: TimeTrackingEntry): F[Unit] =
    run(timeTrackingEntries += toRow(entry)).void

  override def findById(id: TimeTrackingEntryId): F[Option[TimeTrackingEntry]] =
    run(
      timeTrackingEntries
        .filter(_.id === id.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def listByTicket(ticketId: TicketId): F[List[TimeTrackingEntry]] =
    run(
      timeTrackingEntries
        .filter(_.ticketId === ticketId.value)
        .sortBy(_.loggedAt.desc)
        .result
    ).map(_.toList.map(toDomain))

  override def listByUser(userId: UserId): F[List[TimeTrackingEntry]] =
    run(
      timeTrackingEntries
        .filter(_.userId === userId.value)
        .sortBy(_.loggedAt.desc)
        .result
    ).map(_.toList.map(toDomain))

  override def update(entry: TimeTrackingEntry): F[Boolean] =
    run(
      timeTrackingEntries
        .filter(_.id === entry.id.value)
        .update(toRow(entry))
    ).map(_ > 0)

  override def delete(id: TimeTrackingEntryId): F[Boolean] =
    run(
      timeTrackingEntries
        .filter(_.id === id.value)
        .delete
    ).map(_ > 0)

  private def toRow(entry: TimeTrackingEntry): TimeTrackingRow =
    TimeTrackingRow(
      id = entry.id.value,
      ticketId = entry.ticketId.value,
      userId = entry.userId.value,
      activityId = entry.activityId.value,
      durationMinutes = entry.durationMinutes.value,
      loggedAt = entry.loggedAt,
      description = entry.description.map(_.value)
    )

  private def toDomain(row: TimeTrackingRow): TimeTrackingEntry =
    TimeTrackingEntry(
      id = TimeTrackingEntryId(row.id),
      ticketId = TicketId(row.ticketId),
      userId = UserId(row.userId),
      activityId = TimeTrackingActivityId(row.activityId),
      durationMinutes = TimeTrackingDurationMinutes(row.durationMinutes),
      loggedAt = row.loggedAt,
      description = row.description.map(TimeTrackingEntryDescription(_))
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))
}
