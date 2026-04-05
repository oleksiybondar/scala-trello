package io.github.oleksiybondar.api.infrastructure.db.timeTracking

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.timeTracking.{
  TimeTrackingActivity,
  TimeTrackingActivityCode,
  TimeTrackingActivityId,
  TimeTrackingActivityName
}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

final class SlickTimeTrackingActivityRepo[F[_]: Async](db: Database)
    extends TimeTrackingActivityRepo[F] {

  private final case class TimeTrackingActivityRow(
      id: Long,
      code: String,
      name: String,
      description: Option[String]
  )

  private final class ActivitiesTable(tag: Tag)
      extends Table[TimeTrackingActivityRow](tag, "activities") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def code: Rep[String]                = column[String]("code")
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[TimeTrackingActivityRow] =
      (id, code, name, description).mapTo[TimeTrackingActivityRow]
  }

  private val activities = TableQuery[ActivitiesTable]

  private def toDomain(row: TimeTrackingActivityRow): TimeTrackingActivity =
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
