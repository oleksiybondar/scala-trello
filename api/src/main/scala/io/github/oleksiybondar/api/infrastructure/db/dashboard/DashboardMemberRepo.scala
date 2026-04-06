package io.github.oleksiybondar.api.infrastructure.db.dashboard

import io.github.oleksiybondar.api.domain.dashboard.{DashboardId, DashboardMember}
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId

trait DashboardMemberRepo[F[_]] {
  def create(member: DashboardMember): F[Unit]
  def findByDashboardIdAndUserId(
      dashboardId: DashboardId,
      userId: UserId
  ): F[Option[DashboardMember]]
  def listByDashboardId(dashboardId: DashboardId): F[List[DashboardMember]]
  def listByUserId(userId: UserId): F[List[DashboardMember]]
  def updateRole(dashboardId: DashboardId, userId: UserId, roleId: RoleId): F[Boolean]
  def delete(dashboardId: DashboardId, userId: UserId): F[Boolean]
}
