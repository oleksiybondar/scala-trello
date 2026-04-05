package io.github.oleksiybondar.api.infrastructure.db.dashboard

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.dashboard.{
  Dashboard,
  DashboardDescription,
  DashboardId,
  DashboardName
}
import io.github.oleksiybondar.api.domain.user.UserId
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

final class SlickDashboardRepo[F[_]: Async](db: Database) extends DashboardRepo[F] {

  private final case class DashboardRow(
      id: UUID,
      name: String,
      description: Option[String],
      active: Boolean,
      ownerUserId: UUID,
      createdByUserId: UUID,
      createdAt: Instant,
      modifiedAt: Instant,
      lastModifiedByUserId: UUID
  )

  private final class DashboardsTable(tag: Tag)
      extends Table[DashboardRow](tag, "dashboards") {
    def id: Rep[UUID]                    = column[UUID]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")
    def active: Rep[Boolean]             = column[Boolean]("active")
    def ownerUserId: Rep[UUID]           = column[UUID]("owner_user_id")
    def createdByUserId: Rep[UUID]       = column[UUID]("created_by_user_id")
    def createdAt: Rep[Instant]          = column[Instant]("created_at")
    def modifiedAt: Rep[Instant]         = column[Instant]("modified_at")
    def lastModifiedByUserId: Rep[UUID]  = column[UUID]("last_modified_by_user_id")

    def * : ProvenShape[DashboardRow] =
      (
        id,
        name,
        description,
        active,
        ownerUserId,
        createdByUserId,
        createdAt,
        modifiedAt,
        lastModifiedByUserId
      ).mapTo[DashboardRow]
  }

  private val dashboards = TableQuery[DashboardsTable]

  private def toRow(dashboard: Dashboard): DashboardRow =
    DashboardRow(
      id = dashboard.id.value,
      name = dashboard.name.value,
      description = dashboard.description.map(_.value),
      active = dashboard.active,
      ownerUserId = dashboard.ownerUserId.value,
      createdByUserId = dashboard.createdByUserId.value,
      createdAt = dashboard.createdAt,
      modifiedAt = dashboard.modifiedAt,
      lastModifiedByUserId = dashboard.lastModifiedByUserId.value
    )

  private def toDomain(row: DashboardRow): Dashboard =
    Dashboard(
      id = DashboardId(row.id),
      name = DashboardName(row.name),
      description = row.description.map(DashboardDescription(_)),
      active = row.active,
      ownerUserId = UserId(row.ownerUserId),
      createdByUserId = UserId(row.createdByUserId),
      createdAt = row.createdAt,
      modifiedAt = row.modifiedAt,
      lastModifiedByUserId = UserId(row.lastModifiedByUserId)
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))

  override def create(dashboard: Dashboard): F[Unit] =
    run(dashboards += toRow(dashboard)).void

  override def findById(id: DashboardId): F[Option[Dashboard]] =
    run(
      dashboards
        .filter(_.id === id.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def list: F[List[Dashboard]] =
    run(
      dashboards
        .sortBy(_.createdAt.desc)
        .result
    ).map(_.toList.map(toDomain))

  override def listByOwner(ownerUserId: UserId): F[List[Dashboard]] =
    run(
      dashboards
        .filter(_.ownerUserId === ownerUserId.value)
        .sortBy(_.createdAt.desc)
        .result
    ).map(_.toList.map(toDomain))

  override def update(dashboard: Dashboard): F[Boolean] =
    run(
      dashboards
        .filter(_.id === dashboard.id.value)
        .update(toRow(dashboard))
    ).map(_ > 0)

  override def delete(id: DashboardId): F[Boolean] =
    run(
      dashboards
        .filter(_.id === id.value)
        .delete
    ).map(_ > 0)
}
