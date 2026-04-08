package io.github.oleksiybondar.api.infrastructure.db.board

import io.github.oleksiybondar.api.domain.board.{BoardId, BoardMember}
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId

trait BoardMemberRepo[F[_]] {
  def create(member: BoardMember): F[Unit]
  def findByDashboardIdAndUserId(
      dashboardId: BoardId,
      userId: UserId
  ): F[Option[BoardMember]]
  def listByDashboardId(dashboardId: BoardId): F[List[BoardMember]]
  def listByUserId(userId: UserId): F[List[BoardMember]]
  def updateRole(dashboardId: BoardId, userId: UserId, roleId: RoleId): F[Boolean]
  def delete(dashboardId: BoardId, userId: UserId): F[Boolean]
}
