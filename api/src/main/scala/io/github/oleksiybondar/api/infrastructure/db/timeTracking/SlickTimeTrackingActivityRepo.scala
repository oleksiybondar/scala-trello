package io.github.oleksiybondar.api.infrastructure.db.timeTracking

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.timeTracking.{
  TimeTrackingActivity,
  TimeTrackingActivityCode,
  TimeTrackingActivityId,
  TimeTrackingActivityName
}
import io.github.oleksiybondar.api.infrastructure.db.SharedSlickTables.{
  ActivitiesTable,
  ActivityRow
}
import slick.jdbc.PostgresProfile.api._

final class SlickTimeTrackingActivityRepo[F[_]: Async](db: Database)
    extends TimeTrackingActivityRepo[F] {

  private val activities = TableQuery[ActivitiesTable]

  private def toDomain(row: ActivityRow): TimeTrackingActivity =
    TimeTrackingActivity(
      id = TimeTrackingActivityId(row.id),
      code = TimeTrackingActivityCode(row.code),
      name = TimeTrackingActivityName(row.name),
      description = row.description
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))

  override def findById(id: TimeTrackingActivityId): F[Option[TimeTrackingActivity]] =
    run(
      activities
        .filter(_.id === id.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def findByCode(code: TimeTrackingActivityCode): F[Option[TimeTrackingActivity]] =
    run(
      activities
        .filter(_.code === code.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def list: F[List[TimeTrackingActivity]] =
    run(
      activities
        .sortBy(_.id.asc)
        .result
    ).map(_.toList.map(toDomain))
}
