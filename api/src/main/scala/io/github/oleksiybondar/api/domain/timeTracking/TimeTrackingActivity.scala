package io.github.oleksiybondar.api.domain.timeTracking

final case class TimeTrackingActivityId(value: Long)     extends AnyVal
final case class TimeTrackingActivityCode(value: String) extends AnyVal
final case class TimeTrackingActivityName(value: String) extends AnyVal

final case class TimeTrackingActivity(
    id: TimeTrackingActivityId,
    code: TimeTrackingActivityCode,
    name: TimeTrackingActivityName,
    description: Option[String]
)
