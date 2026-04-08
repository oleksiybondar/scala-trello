package io.github.oleksiybondar.api.domain.board

import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId

/** Service responsible for dashboard membership lifecycle and membership reads enriched with role
  * data.
  *
  * This service owns add/remove/change-role operations for dashboard members and returns
  * `BoardMemberWithRole` for read operations.
  *
  * Authorization decisions are intentionally not handled here; those belong to
  * `BoardAccessService`.
  */
trait BoardMembershipService[F[_]] {

  /** Persists a new dashboard membership as-is. */
  def addMember(member: BoardMember): F[Unit]

  /** Removes a dashboard membership identified by dashboard id and user id. */
  def removeMember(dashboardId: BoardId, userId: UserId): F[Boolean]

  /** Updates the assigned role for an existing dashboard membership. */
  def changeMemberRole(dashboardId: BoardId, userId: UserId, roleId: RoleId): F[Boolean]

  /** Loads a single dashboard membership and enriches it with the resolved role permissions. */
  def findMember(dashboardId: BoardId, userId: UserId): F[Option[BoardMemberWithRole]]

  /** Lists all members for a dashboard and enriches each membership with role permissions. */
  def listMembers(dashboardId: BoardId): F[List[BoardMemberWithRole]]

  /** Lists all memberships for a user and enriches each membership with role permissions. */
  def listMembershipsForUser(userId: UserId): F[List[BoardMemberWithRole]]
}
