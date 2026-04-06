package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.dashboard.{DashboardId, DashboardMember}
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.dashboard.DashboardMemberRepo

final class InMemoryDashboardMemberRepo[F[_]: Sync] private (
    state: Ref[F, Map[(DashboardId, UserId), DashboardMember]]
) extends DashboardMemberRepo[F] {

  override def create(member: DashboardMember): F[Unit] =
    state.update(_.updated((member.dashboardId, member.userId), member))

  override def findByDashboardIdAndUserId(
      dashboardId: DashboardId,
      userId: UserId
  ): F[Option[DashboardMember]] =
    state.get.map(_.get((dashboardId, userId)))

  override def listByDashboardId(dashboardId: DashboardId): F[List[DashboardMember]] =
    state.get.map(
      _.values.toList
        .filter(_.dashboardId == dashboardId)
        .sortBy(_.createdAt)
    )

  override def listByUserId(userId: UserId): F[List[DashboardMember]] =
    state.get.map(
      _.values.toList
        .filter(_.userId == userId)
        .sortBy(_.createdAt)
    )

  override def updateRole(
      dashboardId: DashboardId,
      userId: UserId,
      roleId: RoleId
  ): F[Boolean] =
    state.modify { current =>
      current.get((dashboardId, userId)) match {
        case Some(existing) =>
          (
            current.updated(
              (dashboardId, userId),
              existing.copy(roleId = roleId)
            ),
            true
          )
        case None           => (current, false)
      }
    }

  override def delete(dashboardId: DashboardId, userId: UserId): F[Boolean] =
    state.modify { current =>
      if (current.contains((dashboardId, userId)))
        (current - ((dashboardId, userId)), true)
      else
        (current, false)
    }
}

object InMemoryDashboardMemberRepo {

  def create[F[_]: Sync](
      members: List[DashboardMember] = Nil
  ): F[InMemoryDashboardMemberRepo[F]] =
    Ref
      .of[F, Map[(DashboardId, UserId), DashboardMember]](
        members.map(member => (member.dashboardId, member.userId) -> member).toMap
      )
      .map(new InMemoryDashboardMemberRepo[F](_))
}
