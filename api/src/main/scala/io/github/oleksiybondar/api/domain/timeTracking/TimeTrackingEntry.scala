package io.github.oleksiybondar.api.domain.timeTracking

import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant

final case class TimeTrackingEntryId(value: Long)            extends AnyVal
final case class TimeTrackingDurationMinutes(value: Int)     extends AnyVal
final case class TimeTrackingEntryDescription(value: String) extends AnyVal

final case class TimeTrackingEntry(
    id: TimeTrackingEntryId,
    ticketId: TicketId,
    userId: UserId,
    activityId: TimeTrackingActivityId,
    durationMinutes: TimeTrackingDurationMinutes,
    loggedAt: Instant,
    description: Option[TimeTrackingEntryDescription]
)
