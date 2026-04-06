package io.github.oleksiybondar.api.domain.dashboard

import cats.Monad
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.permission.PermissionArea
import io.github.oleksiybondar.api.domain.user.UserId

final class DashboardAccessServiceLive[F[_]: Monad](
    dashboardMembershipService: DashboardMembershipService[F]
) extends DashboardAccessService[F] {

  override def canRead(dashboardId: DashboardId, userId: UserId, area: PermissionArea): F[Boolean] =
    checkPermission(dashboardId, userId)(_.canRead(area))

  override def canCreate(
      dashboardId: DashboardId,
      userId: UserId,
      area: PermissionArea
  ): F[Boolean] =
    checkPermission(dashboardId, userId)(_.canCreate(area))

  override def canModify(
      dashboardId: DashboardId,
      userId: UserId,
      area: PermissionArea
  ): F[Boolean] =
    checkPermission(dashboardId, userId)(_.canModify(area))

  override def canDelete(
      dashboardId: DashboardId,
      userId: UserId,
      area: PermissionArea
  ): F[Boolean] =
    checkPermission(dashboardId, userId)(_.canDelete(area))

  override def canReassign(
      dashboardId: DashboardId,
      userId: UserId,
      area: PermissionArea
  ): F[Boolean] =
    checkPermission(dashboardId, userId)(_.canReassign(area))

  override def canReadDashboard(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canRead(dashboardId, userId, PermissionArea.Dashboard)

  override def canCreateDashboard(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canCreate(dashboardId, userId, PermissionArea.Dashboard)

  override def canModifyDashboard(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canModify(dashboardId, userId, PermissionArea.Dashboard)

  override def canDeleteDashboard(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canDelete(dashboardId, userId, PermissionArea.Dashboard)

  override def canReassignDashboard(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canReassign(dashboardId, userId, PermissionArea.Dashboard)

  override def canReadTicket(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canRead(dashboardId, userId, PermissionArea.Ticket)

  override def canCreateTicket(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canCreate(dashboardId, userId, PermissionArea.Ticket)

  override def canModifyTicket(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canModify(dashboardId, userId, PermissionArea.Ticket)

  override def canDeleteTicket(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canDelete(dashboardId, userId, PermissionArea.Ticket)

  override def canReassignTicket(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canReassign(dashboardId, userId, PermissionArea.Ticket)

  override def canReadComment(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canRead(dashboardId, userId, PermissionArea.Comment)

  override def canCreateComment(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canCreate(dashboardId, userId, PermissionArea.Comment)

  override def canModifyComment(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canModify(dashboardId, userId, PermissionArea.Comment)

  override def canDeleteComment(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canDelete(dashboardId, userId, PermissionArea.Comment)

  override def canReassignComment(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    canReassign(dashboardId, userId, PermissionArea.Comment)

  private def checkPermission(
      dashboardId: DashboardId,
      userId: UserId
  )(evaluate: DashboardMemberWithRole => Boolean): F[Boolean] =
    dashboardMembershipService
      .findMember(dashboardId, userId)
      .map(_.exists(evaluate))
}
