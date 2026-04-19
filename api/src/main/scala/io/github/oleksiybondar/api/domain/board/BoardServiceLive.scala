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
                       boardId = dashboard.id,
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

  override def listDashboardsForUser(
      userId: UserId,
      filters: BoardQueryFilters
  ): F[List[Board]] =
    dashboardMembershipService
      .listMembershipsForUser(userId)
      .flatMap(
        _.traverse(memberWithRole => dashboardRepo.findById(memberWithRole.member.boardId))
      )
      .map(_.flatten)
      .map(_.filter(matchesFilters(_, filters)))
      .map(_.sortBy(_.modifiedAt)(Ordering[java.time.Instant].reverse))

  override def listDashboardsForUserPage(
      userId: UserId,
      filters: BoardQueryFilters,
      offset: Int,
      limit: Int
  ): F[List[Board]] =
    listDashboardsForUser(userId, filters).map(paginate(_, offset, limit))

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
                          boardId = dashboardId,
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

  override def changeTitle(
      dashboardId: BoardId,
      actorUserId: UserId,
      title: BoardName
  ): F[Boolean] =
    updateDashboardMetadata(dashboardId, actorUserId) { dashboard =>
      dashboard.copy(name = title)
    }

  override def changeDescription(
      dashboardId: BoardId,
      actorUserId: UserId,
      description: Option[BoardDescription]
  ): F[Boolean] =
    updateDashboardMetadata(dashboardId, actorUserId) { dashboard =>
      dashboard.copy(description = description)
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

  override def activate(dashboardId: BoardId, actorUserId: UserId): F[Boolean] =
    dashboardAccessService.canModifyDashboard(dashboardId, actorUserId).flatMap {
      case false => false.pure[F]
      case true  =>
        dashboardRepo.findById(dashboardId).flatMap {
          case None            => false.pure[F]
          case Some(dashboard) =>
            Temporal[F].realTimeInstant.flatMap { now =>
              dashboardRepo.update(
                dashboard.copy(
                  active = true,
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
                        boardId = dashboardId,
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

  private def updateDashboardMetadata(
      dashboardId: BoardId,
      actorUserId: UserId
  )(update: Board => Board): F[Boolean] =
    dashboardAccessService.canModifyDashboard(dashboardId, actorUserId).flatMap {
      case false => false.pure[F]
      case true  =>
        dashboardRepo.findById(dashboardId).flatMap {
          case None            => false.pure[F]
          case Some(dashboard) =>
            Temporal[F].realTimeInstant.flatMap { now =>
              dashboardRepo.update(
                update(dashboard).copy(
                  modifiedAt = now,
                  lastModifiedByUserId = actorUserId
                )
              )
            }
        }
    }

  private def matchesFilters(board: Board, filters: BoardQueryFilters): Boolean = {
    val matchesActive     = filters.active.forall(_ == board.active)
    val normalizedKeyword = filters.keyword.map(_.trim.toLowerCase).filter(_.nonEmpty)
    val matchesKeyword    = normalizedKeyword.forall { keyword =>
      board.name.value.toLowerCase.contains(keyword) ||
      board.description.exists(_.value.toLowerCase.contains(keyword))
    }
    val matchesOwner      = filters.ownerUserId.forall(_ == board.ownerUserId)

    matchesActive && matchesKeyword && matchesOwner
  }

  private def paginate[A](items: List[A], offset: Int, limit: Int): List[A] = {
    val normalizedOffset = math.max(0, offset)
    val normalizedLimit  = math.max(0, limit)

    items.slice(normalizedOffset, normalizedOffset + normalizedLimit)
  }
}
