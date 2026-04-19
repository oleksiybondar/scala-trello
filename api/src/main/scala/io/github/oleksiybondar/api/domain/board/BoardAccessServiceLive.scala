package io.github.oleksiybondar.api.domain.board

import cats.Monad
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.permission.PermissionArea
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.board.BoardRepo

final class BoardAccessServiceLive[F[_]: Monad](
    dashboardRepo: BoardRepo[F],
    dashboardMembershipService: BoardMembershipService[F]
) extends BoardAccessService[F] {

  override def canRead(dashboardId: BoardId, userId: UserId, area: PermissionArea): F[Boolean] =
    checkPermission(dashboardId, userId, allowInactiveRead = true)(_.canRead(area))

  override def canCreate(
      dashboardId: BoardId,
      userId: UserId,
      area: PermissionArea
  ): F[Boolean] =
    checkPermission(dashboardId, userId, allowInactiveRead = false)(_.canCreate(area))

  override def canModify(
      dashboardId: BoardId,
      userId: UserId,
      area: PermissionArea
  ): F[Boolean] =
    checkPermission(dashboardId, userId, allowInactiveRead = false)(_.canModify(area))

  override def canDelete(
      dashboardId: BoardId,
      userId: UserId,
      area: PermissionArea
  ): F[Boolean] =
    checkPermission(dashboardId, userId, allowInactiveRead = false)(_.canDelete(area))

  override def canReassign(
      dashboardId: BoardId,
      userId: UserId,
      area: PermissionArea
  ): F[Boolean] =
    checkPermission(dashboardId, userId, allowInactiveRead = false)(_.canReassign(area))

  override def canReadDashboard(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canRead(dashboardId, userId, PermissionArea.Board)

  override def canCreateDashboard(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canCreate(dashboardId, userId, PermissionArea.Board)

  override def canModifyDashboard(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canModify(dashboardId, userId, PermissionArea.Board)

  override def canDeleteDashboard(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canDelete(dashboardId, userId, PermissionArea.Board)

  override def canReassignDashboard(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canReassign(dashboardId, userId, PermissionArea.Board)

  override def canReadTicket(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canRead(dashboardId, userId, PermissionArea.Ticket)

  override def canCreateTicket(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canCreate(dashboardId, userId, PermissionArea.Ticket)

  override def canModifyTicket(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canModify(dashboardId, userId, PermissionArea.Ticket)

  override def canDeleteTicket(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canDelete(dashboardId, userId, PermissionArea.Ticket)

  override def canReassignTicket(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canReassign(dashboardId, userId, PermissionArea.Ticket)

  override def canReadComment(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canRead(dashboardId, userId, PermissionArea.Comment)

  override def canCreateComment(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canCreate(dashboardId, userId, PermissionArea.Comment)

  override def canModifyComment(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canModify(dashboardId, userId, PermissionArea.Comment)

  override def canDeleteComment(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canDelete(dashboardId, userId, PermissionArea.Comment)

  override def canReassignComment(dashboardId: BoardId, userId: UserId): F[Boolean] =
    canReassign(dashboardId, userId, PermissionArea.Comment)

  private def checkPermission(
      dashboardId: BoardId,
      userId: UserId,
      allowInactiveRead: Boolean
  )(evaluate: BoardMemberWithRole => Boolean): F[Boolean] =
    dashboardRepo.findById(dashboardId).flatMap {
      case None            => false.pure[F]
      case Some(dashboard) =>
        if (!dashboard.active && dashboard.ownerUserId != userId && !allowInactiveRead) {
          false.pure[F]
        } else
          dashboardMembershipService
            .findMember(dashboardId, userId)
            .map(_.exists(evaluate))
    }
}
