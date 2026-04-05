package io.github.oleksiybondar.api.infrastructure.db.timeTracking

import io.github.oleksiybondar.api.domain.timeTracking.{
  TimeTrackingActivity,
  TimeTrackingActivityCode,
  TimeTrackingActivityId
}

trait TimeTrackingActivityRepo[F[_]] {
  def findById(id: TimeTrackingActivityId): F[Option[TimeTrackingActivity]]
  def findByCode(code: TimeTrackingActivityCode): F[Option[TimeTrackingActivity]]
  def list: F[List[TimeTrackingActivity]]
}
