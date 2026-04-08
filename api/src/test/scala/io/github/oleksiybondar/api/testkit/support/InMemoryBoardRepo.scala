package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.{Board, BoardId}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.board.BoardRepo

final class InMemoryBoardRepo[F[_]: Sync] private (
    state: Ref[F, Map[BoardId, Board]]
) extends BoardRepo[F] {

  override def create(dashboard: Board): F[Unit] =
    state.update(_.updated(dashboard.id, dashboard))

  override def findById(id: BoardId): F[Option[Board]] =
    state.get.map(_.get(id))

  override def list: F[List[Board]] =
    state.get.map(_.values.toList.sortBy(_.createdAt)(Ordering[java.time.Instant].reverse))

  override def listByOwner(ownerUserId: UserId): F[List[Board]] =
    state.get.map(
      _.values.toList
        .filter(_.ownerUserId == ownerUserId)
        .sortBy(_.createdAt)(Ordering[java.time.Instant].reverse)
    )

  override def listByMember(userId: UserId): F[List[Board]] =
    state.get.map(
      _.values.toList
        .filter(dashboard => dashboard.ownerUserId == userId || dashboard.createdByUserId == userId)
        .sortBy(_.createdAt)(Ordering[java.time.Instant].reverse)
    )

  override def update(dashboard: Board): F[Boolean] =
    state.modify { current =>
      if (current.contains(dashboard.id))
        (current.updated(dashboard.id, dashboard), true)
      else
        (current, false)
    }

  override def delete(id: BoardId): F[Boolean] =
    state.modify { current =>
      if (current.contains(id))
        (current - id, true)
      else
        (current, false)
    }
}

object InMemoryBoardRepo {

  def create[F[_]: Sync](dashboards: List[Board] = Nil): F[InMemoryBoardRepo[F]] =
    Ref
      .of[F, Map[BoardId, Board]](dashboards.map(dashboard =>
        dashboard.id -> dashboard
      ).toMap)
      .map(new InMemoryBoardRepo[F](_))
}
