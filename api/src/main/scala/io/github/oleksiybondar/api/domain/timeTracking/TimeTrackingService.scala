package io.github.oleksiybondar.api.domain.timeTracking

import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant

final case class CreateTimeTrackingEntryCommand(
    ticketId: TicketId,
    activityId: TimeTrackingActivityId,
    durationMinutes: TimeTrackingDurationMinutes,
    loggedAt: Instant,
    description: Option[TimeTrackingEntryDescription]
)

final case class UpdateTimeTrackingEntryCommand(
    activityId: TimeTrackingActivityId,
    durationMinutes: TimeTrackingDurationMinutes,
    loggedAt: Instant,
    description: Option[TimeTrackingEntryDescription]
)

trait TimeTrackingService[F[_]] {
  def createEntry(
      command: CreateTimeTrackingEntryCommand,
      actorUserId: UserId
  ): F[Option[TimeTrackingEntry]]

  def getOwnEntry(id: TimeTrackingEntryId, actorUserId: UserId): F[Option[TimeTrackingEntry]]

  def listOwnEntries(actorUserId: UserId): F[List[TimeTrackingEntry]]

  def updateOwnEntry(
      id: TimeTrackingEntryId,
      command: UpdateTimeTrackingEntryCommand,
      actorUserId: UserId
  ): F[Boolean]

  def deleteOwnEntry(id: TimeTrackingEntryId, actorUserId: UserId): F[Boolean]
}
