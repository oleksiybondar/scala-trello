package io.github.oleksiybondar.api.infrastructure.db.timeTracking

import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.timeTracking.{TimeTrackingEntry, TimeTrackingEntryId}
import io.github.oleksiybondar.api.domain.user.UserId

trait TimeTrackingRepo[F[_]] {
  def nextId: F[TimeTrackingEntryId]
  def create(entry: TimeTrackingEntry): F[Unit]
  def findById(id: TimeTrackingEntryId): F[Option[TimeTrackingEntry]]
  def listByTicket(ticketId: TicketId): F[List[TimeTrackingEntry]]
  def listByUser(userId: UserId): F[List[TimeTrackingEntry]]
  def update(entry: TimeTrackingEntry): F[Boolean]
  def delete(id: TimeTrackingEntryId): F[Boolean]
}
