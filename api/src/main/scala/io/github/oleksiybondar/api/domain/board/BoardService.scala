package io.github.oleksiybondar.api.domain.board

import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId

/** Service responsible for dashboard lifecycle operations and membership-facing orchestration.
  *
  * This service owns dashboard CRUD-oriented business actions and delegates authorization checks to
  * `BoardAccessService`. Membership persistence and role enrichment stay in
  * `BoardMembershipService`.
  */
trait BoardService[F[_]] {

  /** Persists a new dashboard and automatically adds the owner as an admin member. */
  def createDashboard(dashboard: Board): F[Unit]

  /** Loads a dashboard by id. */
  def getDashboard(id: BoardId): F[Option[Board]]

  /** Lists all dashboards. */
  def listDashboards: F[List[Board]]

  /** Lists dashboards where the user is a member. */
  def listDashboardsForUser(userId: UserId): F[List[Board]]

  /** Changes dashboard ownership when the acting user has sufficient dashboard rights. */
  def changeOwnership(
      dashboardId: BoardId,
      actorUserId: UserId,
      newOwnerUserId: UserId
  ): F[Boolean]

  /** Deactivates a dashboard when the acting user has sufficient dashboard rights. */
  def deactivate(dashboardId: BoardId, actorUserId: UserId): F[Boolean]

  /** Adds a new dashboard member when the acting user has sufficient dashboard rights. */
  def addMember(
      dashboardId: BoardId,
      actorUserId: UserId,
      memberUserId: UserId,
      roleId: RoleId
  ): F[Boolean]

  /** Changes an existing dashboard member role when the acting user has sufficient dashboard
    * rights.
    */
  def changeMemberRole(
      dashboardId: BoardId,
      actorUserId: UserId,
      memberUserId: UserId,
      roleId: RoleId
  ): F[Boolean]

  /** Removes an existing dashboard member when the acting user has sufficient dashboard rights. */
  def removeMember(dashboardId: BoardId, actorUserId: UserId, memberUserId: UserId): F[Boolean]
}
