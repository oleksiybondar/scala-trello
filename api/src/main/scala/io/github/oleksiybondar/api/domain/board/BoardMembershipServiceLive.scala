package io.github.oleksiybondar.api.domain.board

import cats.MonadThrow
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.permission.{RoleId, RoleService}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.board.BoardMemberRepo

final class BoardMembershipServiceLive[F[_]: MonadThrow](
    boardMemberRepo: BoardMemberRepo[F],
    roleService: RoleService[F]
) extends BoardMembershipService[F] {

  override def addMember(member: BoardMember): F[Unit] =
    boardMemberRepo.create(member)

  override def removeMember(dashboardId: BoardId, userId: UserId): F[Boolean] =
    boardMemberRepo.delete(dashboardId, userId)

  override def changeMemberRole(
      dashboardId: BoardId,
      userId: UserId,
      roleId: RoleId
  ): F[Boolean] =
    boardMemberRepo.updateRole(dashboardId, userId, roleId)

  override def findMember(
      dashboardId: BoardId,
      userId: UserId
  ): F[Option[BoardMemberWithRole]] =
    boardMemberRepo
      .findByBoardIdAndUserId(dashboardId, userId)
      .flatMap(_.traverse(toMemberWithRole))

  override def listMembers(dashboardId: BoardId): F[List[BoardMemberWithRole]] =
    boardMemberRepo
      .listByBoardId(dashboardId)
      .flatMap(_.traverse(toMemberWithRole))

  override def listMembershipsForUser(userId: UserId): F[List[BoardMemberWithRole]] =
    boardMemberRepo
      .listByUserId(userId)
      .flatMap(_.traverse(toMemberWithRole))

  private def toMemberWithRole(member: BoardMember): F[BoardMemberWithRole] =
    roleService
      .getRoleWithPermissions(member.roleId)
      .flatMap(
        _.liftTo[F](new IllegalStateException(s"Role ${member.roleId.value} was not found"))
      )
      .map(role => BoardMemberWithRole(member, role))
}
