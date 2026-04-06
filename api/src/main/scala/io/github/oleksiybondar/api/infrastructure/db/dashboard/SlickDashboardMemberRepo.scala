package io.github.oleksiybondar.api.infrastructure.db.dashboard

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.dashboard.{DashboardId, DashboardMember}
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{PrimaryKey, ProvenShape}

import java.time.Instant
import java.util.UUID

final class SlickDashboardMemberRepo[F[_]: Async](db: Database) extends DashboardMemberRepo[F] {

  private final case class DashboardMemberRow(
      dashboardId: UUID,
      userId: UUID,
      roleId: Long,
      createdAt: Instant
  )

  private final class DashboardMembersTable(tag: Tag)
      extends Table[DashboardMemberRow](tag, "dashboard_members") {
    def dashboardId: Rep[UUID]  = column[UUID]("dashboard_id")
    def userId: Rep[UUID]       = column[UUID]("user_id")
    def roleId: Rep[Long]       = column[Long]("role_id")
    def createdAt: Rep[Instant] = column[Instant]("created_at")

    def pk: PrimaryKey = primaryKey("dashboard_members_pk", (dashboardId, userId))

    def * : ProvenShape[DashboardMemberRow] =
      (dashboardId, userId, roleId, createdAt).mapTo[DashboardMemberRow]
  }

  private val dashboardMembers = TableQuery[DashboardMembersTable]

  private def toRow(member: DashboardMember): DashboardMemberRow =
    DashboardMemberRow(
      dashboardId = member.dashboardId.value,
      userId = member.userId.value,
      roleId = member.roleId.value,
      createdAt = member.createdAt
    )

  private def toDomain(row: DashboardMemberRow): DashboardMember =
    DashboardMember(
      dashboardId = DashboardId(row.dashboardId),
      userId = UserId(row.userId),
      roleId = RoleId(row.roleId),
      createdAt = row.createdAt
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))

  override def create(member: DashboardMember): F[Unit] =
    run(dashboardMembers += toRow(member)).void

  override def findByDashboardIdAndUserId(
      dashboardId: DashboardId,
      userId: UserId
  ): F[Option[DashboardMember]] =
    run(
      dashboardMembers
        .filter(row => row.dashboardId === dashboardId.value && row.userId === userId.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def listByDashboardId(dashboardId: DashboardId): F[List[DashboardMember]] =
    run(
      dashboardMembers
        .filter(_.dashboardId === dashboardId.value)
        .sortBy(_.createdAt.asc)
        .result
    ).map(_.toList.map(toDomain))

  override def listByUserId(userId: UserId): F[List[DashboardMember]] =
    run(
      dashboardMembers
        .filter(_.userId === userId.value)
        .sortBy(_.createdAt.asc)
        .result
    ).map(_.toList.map(toDomain))

  override def updateRole(
      dashboardId: DashboardId,
      userId: UserId,
      roleId: RoleId
  ): F[Boolean] =
    run(
      dashboardMembers
        .filter(row => row.dashboardId === dashboardId.value && row.userId === userId.value)
        .map(_.roleId)
        .update(roleId.value)
    ).map(_ > 0)

  override def delete(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    run(
      dashboardMembers
        .filter(row => row.dashboardId === dashboardId.value && row.userId === userId.value)
        .delete
    ).map(_ > 0)
}
