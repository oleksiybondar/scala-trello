package io.github.oleksiybondar.api.domain.dashboard

import io.github.oleksiybondar.api.domain.permission.PermissionArea
import io.github.oleksiybondar.api.domain.user.UserId

/** Service responsible for evaluating dashboard access based on dashboard state and membership role
  * permissions.
  *
  * This service first evaluates whether the dashboard itself allows access:
  *   - missing dashboards deny access
  *   - active dashboards continue to membership-based permission checks
  *   - inactive dashboards deny access unless the requester is the owner
  *
  * After the dashboard-level gate is passed, permission decisions are driven by the assigned
  * membership role. Missing membership is treated as denied access.
  */
trait DashboardAccessService[F[_]] {

  /** Returns whether the user may read resources in the given permission area. */
  def canRead(dashboardId: DashboardId, userId: UserId, area: PermissionArea): F[Boolean]

  /** Returns whether the user may create resources in the given permission area. */
  def canCreate(dashboardId: DashboardId, userId: UserId, area: PermissionArea): F[Boolean]

  /** Returns whether the user may modify resources in the given permission area. */
  def canModify(dashboardId: DashboardId, userId: UserId, area: PermissionArea): F[Boolean]

  /** Returns whether the user may delete resources in the given permission area. */
  def canDelete(dashboardId: DashboardId, userId: UserId, area: PermissionArea): F[Boolean]

  /** Returns whether the user may reassign resources in the given permission area. */
  def canReassign(dashboardId: DashboardId, userId: UserId, area: PermissionArea): F[Boolean]

  /** Convenience wrapper for dashboard read access. */
  def canReadDashboard(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for dashboard creation capabilities. */
  def canCreateDashboard(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for dashboard modification access. */
  def canModifyDashboard(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for dashboard deletion access. */
  def canDeleteDashboard(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for dashboard reassignment capabilities. */
  def canReassignDashboard(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for ticket read access. */
  def canReadTicket(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for ticket creation capabilities. */
  def canCreateTicket(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for ticket modification access. */
  def canModifyTicket(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for ticket deletion access. */
  def canDeleteTicket(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for ticket reassignment capabilities. */
  def canReassignTicket(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for comment read access. */
  def canReadComment(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for comment creation capabilities. */
  def canCreateComment(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for comment modification access. */
  def canModifyComment(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for comment deletion access. */
  def canDeleteComment(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Convenience wrapper for comment reassignment capabilities. */
  def canReassignComment(dashboardId: DashboardId, userId: UserId): F[Boolean]
}
