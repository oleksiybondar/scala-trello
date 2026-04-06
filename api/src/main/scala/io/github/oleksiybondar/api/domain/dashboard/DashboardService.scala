package io.github.oleksiybondar.api.domain.dashboard

import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId

/** Service responsible for dashboard lifecycle operations and membership-facing orchestration.
  *
  * This service owns dashboard CRUD-oriented business actions and delegates authorization checks to
  * `DashboardAccessService`. Membership persistence and role enrichment stay in
  * `DashboardMembershipService`.
  */
trait DashboardService[F[_]] {

  /** Persists a new dashboard and automatically adds the owner as an admin member. */
  def createDashboard(dashboard: Dashboard): F[Unit]

  /** Loads a dashboard by id. */
  def getDashboard(id: DashboardId): F[Option[Dashboard]]

  /** Lists all dashboards. */
  def listDashboards: F[List[Dashboard]]

  /** Lists dashboards where the user is a member. */
  def listDashboardsForUser(userId: UserId): F[List[Dashboard]]

  /** Changes dashboard ownership when the acting user has sufficient dashboard rights. */
  def changeOwnership(
      dashboardId: DashboardId,
      actorUserId: UserId,
      newOwnerUserId: UserId
  ): F[Boolean]

  /** Deactivates a dashboard when the acting user has sufficient dashboard rights. */
  def deactivate(dashboardId: DashboardId, actorUserId: UserId): F[Boolean]

  /** Adds a new dashboard member when the acting user has sufficient dashboard rights. */
  def addMember(
      dashboardId: DashboardId,
      actorUserId: UserId,
      memberUserId: UserId,
      roleId: RoleId
  ): F[Boolean]

  /** Removes an existing dashboard member when the acting user has sufficient dashboard rights. */
  def removeMember(dashboardId: DashboardId, actorUserId: UserId, memberUserId: UserId): F[Boolean]
}
