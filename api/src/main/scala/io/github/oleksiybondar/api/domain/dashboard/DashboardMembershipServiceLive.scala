package io.github.oleksiybondar.api.domain.dashboard

import cats.MonadThrow
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.permission.{RoleId, RoleService}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.dashboard.DashboardMemberRepo

final class DashboardMembershipServiceLive[F[_]: MonadThrow](
    dashboardMemberRepo: DashboardMemberRepo[F],
    roleService: RoleService[F]
) extends DashboardMembershipService[F] {

  override def addMember(member: DashboardMember): F[Unit] =
    dashboardMemberRepo.create(member)

  override def removeMember(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    dashboardMemberRepo.delete(dashboardId, userId)

  override def changeMemberRole(
      dashboardId: DashboardId,
      userId: UserId,
      roleId: RoleId
  ): F[Boolean] =
    dashboardMemberRepo.updateRole(dashboardId, userId, roleId)

  override def findMember(
      dashboardId: DashboardId,
      userId: UserId
  ): F[Option[DashboardMemberWithRole]] =
    dashboardMemberRepo
      .findByDashboardIdAndUserId(dashboardId, userId)
      .flatMap(_.traverse(toMemberWithRole))

  override def listMembers(dashboardId: DashboardId): F[List[DashboardMemberWithRole]] =
    dashboardMemberRepo
      .listByDashboardId(dashboardId)
      .flatMap(_.traverse(toMemberWithRole))

  override def listMembershipsForUser(userId: UserId): F[List[DashboardMemberWithRole]] =
    dashboardMemberRepo
      .listByUserId(userId)
      .flatMap(_.traverse(toMemberWithRole))

  private def toMemberWithRole(member: DashboardMember): F[DashboardMemberWithRole] =
    roleService
      .getRoleWithPermissions(member.roleId)
      .flatMap(
        _.liftTo[F](new IllegalStateException(s"Role ${member.roleId.value} was not found"))
      )
      .map(role => DashboardMemberWithRole(member, role))
}
