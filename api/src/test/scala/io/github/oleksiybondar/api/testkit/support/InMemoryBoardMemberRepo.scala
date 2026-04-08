package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.{BoardId, BoardMember}
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.board.BoardMemberRepo

final class InMemoryBoardMemberRepo[F[_]: Sync] private (
    state: Ref[F, Map[(BoardId, UserId), BoardMember]]
) extends BoardMemberRepo[F] {

  override def create(member: BoardMember): F[Unit] =
    state.update(_.updated((member.dashboardId, member.userId), member))

  override def findByDashboardIdAndUserId(
      dashboardId: BoardId,
      userId: UserId
  ): F[Option[BoardMember]] =
    state.get.map(_.get((dashboardId, userId)))

  override def listByDashboardId(dashboardId: BoardId): F[List[BoardMember]] =
    state.get.map(
      _.values.toList
        .filter(_.dashboardId == dashboardId)
        .sortBy(_.createdAt)
    )

  override def listByUserId(userId: UserId): F[List[BoardMember]] =
    state.get.map(
      _.values.toList
        .filter(_.userId == userId)
        .sortBy(_.createdAt)
    )

  override def updateRole(
      dashboardId: BoardId,
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

  override def delete(dashboardId: BoardId, userId: UserId): F[Boolean] =
    state.modify { current =>
      if (current.contains((dashboardId, userId)))
        (current - ((dashboardId, userId)), true)
      else
        (current, false)
    }
}

object InMemoryBoardMemberRepo {

  def create[F[_]: Sync](
      members: List[BoardMember] = Nil
  ): F[InMemoryBoardMemberRepo[F]] =
    Ref
      .of[F, Map[(BoardId, UserId), BoardMember]](
        members.map(member => (member.dashboardId, member.userId) -> member).toMap
      )
      .map(new InMemoryBoardMemberRepo[F](_))
}
