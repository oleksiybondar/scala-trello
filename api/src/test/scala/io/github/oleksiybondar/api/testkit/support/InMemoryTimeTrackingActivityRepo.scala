package io.github.oleksiybondar.api.testkit.support

import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.timeTracking.{
  TimeTrackingActivity,
  TimeTrackingActivityCode,
  TimeTrackingActivityId
}
import io.github.oleksiybondar.api.infrastructure.db.timeTracking.TimeTrackingActivityRepo

final class InMemoryTimeTrackingActivityRepo[F[_]: Sync](
    activities: List[TimeTrackingActivity]
) extends TimeTrackingActivityRepo[F] {

  override def findById(id: TimeTrackingActivityId): F[Option[TimeTrackingActivity]] =
    activities.find(_.id == id).pure[F]

  override def findByCode(code: TimeTrackingActivityCode): F[Option[TimeTrackingActivity]] =
    activities.find(_.code == code).pure[F]

  override def list: F[List[TimeTrackingActivity]] =
    activities.sortBy(_.id.value).pure[F]
}
