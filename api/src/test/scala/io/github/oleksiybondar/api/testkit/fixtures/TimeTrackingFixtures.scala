package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.timeTracking.{
  TimeTrackingDurationMinutes,
  TimeTrackingEntry,
  TimeTrackingEntryDescription,
  TimeTrackingEntryId
}
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant
import java.util.UUID

object TimeTrackingFixtures {

  val sampleEntry: TimeTrackingEntry =
    TimeTrackingEntry(
      id = TimeTrackingEntryId(1),
      ticketId = TicketFixtures.sampleTicket.id,
      userId = UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
      activityId = TimeTrackingActivityFixtures.developmentActivity.id,
      durationMinutes = TimeTrackingDurationMinutes(90),
      loggedAt = Instant.parse("2026-04-06T10:00:00Z"),
      description = Some(TimeTrackingEntryDescription("Implemented GraphQL ticket queries."))
    )

  def entry(
      id: TimeTrackingEntryId = sampleEntry.id,
      ticketId: io.github.oleksiybondar.api.domain.ticket.TicketId = sampleEntry.ticketId,
      userId: UserId = sampleEntry.userId,
      activityId: io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingActivityId =
        sampleEntry.activityId,
      durationMinutes: TimeTrackingDurationMinutes = sampleEntry.durationMinutes,
      loggedAt: Instant = sampleEntry.loggedAt,
      description: Option[TimeTrackingEntryDescription] = sampleEntry.description
  ): TimeTrackingEntry =
    TimeTrackingEntry(
      id = id,
      ticketId = ticketId,
      userId = userId,
      activityId = activityId,
      durationMinutes = durationMinutes,
      loggedAt = loggedAt,
      description = description
    )
}
