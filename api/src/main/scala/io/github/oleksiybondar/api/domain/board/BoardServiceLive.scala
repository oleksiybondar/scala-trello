package io.github.oleksiybondar.api.domain.board

import cats.effect.kernel.Temporal
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.permission.{RoleId, RoleName, RoleService}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.board.BoardRepo

final class BoardServiceLive[F[_]: Temporal](
    dashboardRepo: BoardRepo[F],
    dashboardAccessService: BoardAccessService[F],
    dashboardMembershipService: BoardMembershipService[F],
    roleService: RoleService[F]
) extends BoardService[F] {

  override def createDashboard(dashboard: Board): F[Unit] =
    for {
      adminRole <- loadAdminRole
      _         <- dashboardRepo.create(dashboard)
      _         <- dashboardMembershipService.addMember(
                     BoardMember(
                       dashboardId = dashboard.id,
                       userId = dashboard.ownerUserId,
                       roleId = adminRole.id,
                       createdAt = dashboard.createdAt
                     )
                   )
    } yield ()

  override def getDashboard(id: BoardId): F[Option[Board]] =
    dashboardRepo.findById(id)

  override def listDashboards: F[List[Board]] =
    dashboardRepo.list

  override def listDashboardsForUser(userId: UserId): F[List[Board]] =
    dashboardMembershipService
      .listMembershipsForUser(userId)
      .flatMap(
        _.traverse(memberWithRole => dashboardRepo.findById(memberWithRole.member.dashboardId))
      )
      .map(_.flatten)

  override def changeOwnership(
      dashboardId: BoardId,
      actorUserId: UserId,
      newOwnerUserId: UserId
  ): F[Boolean] =
    dashboardAccessService.canModifyDashboard(dashboardId, actorUserId).flatMap {
      case false => false.pure[F]
      case true  =>
        (
          for {
            dashboard <- dashboardRepo.findById(dashboardId)
            adminRole <- loadAdminRole.map(Some(_))
            now       <- Temporal[F].realTimeInstant.map(Some(_))
          } yield (dashboard, adminRole, now)
        ).flatMap {
          case (Some(dashboard), Some(adminRole), Some(now)) =>
            val updatedDashboard =
              dashboard.copy(
                ownerUserId = newOwnerUserId,
                modifiedAt = now,
                lastModifiedByUserId = actorUserId
              )

            for {
              updated        <- dashboardRepo.update(updatedDashboard)
              existingMember <- dashboardMembershipService.findMember(dashboardId, newOwnerUserId)
              _              <-
                if (!updated) ().pure[F]
                else
                  existingMember match {
                    case Some(_) =>
                      dashboardMembershipService
                        .changeMemberRole(dashboardId, newOwnerUserId, adminRole.id)
                        .void
                    case None    =>
                      dashboardMembershipService.addMember(
                        BoardMember(
                          dashboardId = dashboardId,
                          userId = newOwnerUserId,
                          roleId = adminRole.id,
                          createdAt = now
                        )
                      )
                  }
            } yield updated
          case _                                             => false.pure[F]
        }
    }

  override def deactivate(dashboardId: BoardId, actorUserId: UserId): F[Boolean] =
    dashboardAccessService.canDeleteDashboard(dashboardId, actorUserId).flatMap {
      case false => false.pure[F]
      case true  =>
        dashboardRepo.findById(dashboardId).flatMap {
          case None            => false.pure[F]
          case Some(dashboard) =>
            Temporal[F].realTimeInstant.flatMap { now =>
              dashboardRepo.update(
                dashboard.copy(
                  active = false,
                  modifiedAt = now,
                  lastModifiedByUserId = actorUserId
                )
              )
            }
        }
    }

  override def addMember(
      dashboardId: BoardId,
      actorUserId: UserId,
      memberUserId: UserId,
      roleId: RoleId
  ): F[Boolean] =
    dashboardAccessService.canCreateDashboard(dashboardId, actorUserId).flatMap {
      case false => false.pure[F]
      case true  =>
        dashboardMembershipService.findMember(dashboardId, memberUserId).flatMap {
          case Some(_) => false.pure[F]
          case None    =>
            roleService.getRole(roleId).flatMap {
              case None       => false.pure[F]
              case Some(role) =>
                Temporal[F].realTimeInstant.flatMap { now =>
                  dashboardMembershipService
                    .addMember(
                      BoardMember(
                        dashboardId = dashboardId,
                        userId = memberUserId,
                        roleId = role.id,
                        createdAt = now
                      )
                    )
                    .as(true)
                }
            }
        }
    }

  override def changeMemberRole(
      dashboardId: BoardId,
      actorUserId: UserId,
      memberUserId: UserId,
      roleId: RoleId
  ): F[Boolean] =
    dashboardAccessService.canModifyDashboard(dashboardId, actorUserId).flatMap {
      case false => false.pure[F]
      case true  =>
        roleService.getRole(roleId).flatMap {
          case None       => false.pure[F]
          case Some(role) =>
            dashboardMembershipService.changeMemberRole(dashboardId, memberUserId, role.id)
        }
    }

  override def removeMember(
      dashboardId: BoardId,
      actorUserId: UserId,
      memberUserId: UserId
  ): F[Boolean] =
    dashboardAccessService.canDeleteDashboard(dashboardId, actorUserId).flatMap {
      case false => false.pure[F]
      case true  => dashboardMembershipService.removeMember(dashboardId, memberUserId)
    }

  private def loadAdminRole: F[io.github.oleksiybondar.api.domain.permission.Role] =
    roleService
      .getByName(RoleName("admin"))
      .flatMap(_.liftTo[F](new IllegalStateException("Admin role was not found")))
}
