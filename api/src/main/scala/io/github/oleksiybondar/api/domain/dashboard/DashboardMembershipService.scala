package io.github.oleksiybondar.api.domain.dashboard

import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId

/** Service responsible for dashboard membership lifecycle and membership reads enriched with role
  * data.
  *
  * This service owns add/remove/change-role operations for dashboard members and returns
  * `DashboardMemberWithRole` for read operations.
  *
  * Authorization decisions are intentionally not handled here; those belong to
  * `DashboardAccessService`.
  */
trait DashboardMembershipService[F[_]] {

  /** Persists a new dashboard membership as-is. */
  def addMember(member: DashboardMember): F[Unit]

  /** Removes a dashboard membership identified by dashboard id and user id. */
  def removeMember(dashboardId: DashboardId, userId: UserId): F[Boolean]

  /** Updates the assigned role for an existing dashboard membership. */
  def changeMemberRole(dashboardId: DashboardId, userId: UserId, roleId: RoleId): F[Boolean]

  /** Loads a single dashboard membership and enriches it with the resolved role permissions. */
  def findMember(dashboardId: DashboardId, userId: UserId): F[Option[DashboardMemberWithRole]]

  /** Lists all members for a dashboard and enriches each membership with role permissions. */
  def listMembers(dashboardId: DashboardId): F[List[DashboardMemberWithRole]]
}
