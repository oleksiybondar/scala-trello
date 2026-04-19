package io.github.oleksiybondar.api.domain.board

import io.github.oleksiybondar.api.domain.permission.PermissionArea
import io.github.oleksiybondar.api.domain.user.UserId

/** Service responsible for evaluating dashboard access based on dashboard state and membership role
  * permissions.
  *
  * This service first evaluates whether the dashboard itself allows access:
  *   - missing dashboards deny access
  *   - active dashboards continue to membership-based permission checks
  *   - inactive dashboards still allow read checks for members
  *   - inactive dashboards deny non-read checks unless the requester is the owner
  *
  * After the dashboard-level gate is passed, permission decisions are driven by the assigned
  * membership role. Missing membership is treated as denied access.
  */
trait BoardAccessService[F[_]] {

  /** Returns whether the user may read resources in the given permission area. */
  def canRead(dashboardId: BoardId, userId: UserId, area: PermissionArea): F[Boolean]

  /** Returns whether the user may create resources in the given permission area. */
  def canCreate(dashboardId: BoardId, userId: UserId, area: PermissionArea): F[Boolean]

  /** Returns whether the user may modify resources in the given permission area. */
  def canModify(dashboardId: BoardId, userId: UserId, area: PermissionArea): F[Boolean]

  /** Returns whether the user may delete resources in the given permission area. */
  def canDelete(dashboardId: BoardId, userId: UserId, area: PermissionArea): F[Boolean]

  /** Returns whether the user may reassign resources in the given permission area. */
  def canReassign(dashboardId: BoardId, userId: UserId, area: PermissionArea): F[Boolean]

  /** Convenience wrapper for dashboard read access. */
  def canReadDashboard(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for dashboard creation capabilities. */
  def canCreateDashboard(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for dashboard modification access. */
  def canModifyDashboard(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for dashboard deletion access. */
  def canDeleteDashboard(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for dashboard reassignment capabilities. */
  def canReassignDashboard(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for ticket read access. */
  def canReadTicket(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for ticket creation capabilities. */
  def canCreateTicket(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for ticket modification access. */
  def canModifyTicket(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for ticket deletion access. */
  def canDeleteTicket(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for ticket reassignment capabilities. */
  def canReassignTicket(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for comment read access. */
  def canReadComment(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for comment creation capabilities. */
  def canCreateComment(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for comment modification access. */
  def canModifyComment(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for comment deletion access. */
  def canDeleteComment(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for comment reassignment capabilities. */
  def canReassignComment(dashboardId: BoardId, userId: UserId): F[Boolean]
}
