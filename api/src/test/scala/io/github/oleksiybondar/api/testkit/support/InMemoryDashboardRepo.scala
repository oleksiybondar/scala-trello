package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.dashboard.{Dashboard, DashboardId}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.dashboard.DashboardRepo

final class InMemoryDashboardRepo[F[_]: Sync] private (
    state: Ref[F, Map[DashboardId, Dashboard]]
) extends DashboardRepo[F] {

  override def create(dashboard: Dashboard): F[Unit] =
    state.update(_.updated(dashboard.id, dashboard))

  override def findById(id: DashboardId): F[Option[Dashboard]] =
    state.get.map(_.get(id))

  override def list: F[List[Dashboard]] =
    state.get.map(_.values.toList.sortBy(_.createdAt)(Ordering[java.time.Instant].reverse))

  override def listByOwner(ownerUserId: UserId): F[List[Dashboard]] =
    state.get.map(
      _.values.toList
        .filter(_.ownerUserId == ownerUserId)
        .sortBy(_.createdAt)(Ordering[java.time.Instant].reverse)
    )

  override def listByMember(userId: UserId): F[List[Dashboard]] =
    state.get.map(
      _.values.toList
        .filter(dashboard => dashboard.ownerUserId == userId || dashboard.createdByUserId == userId)
        .sortBy(_.createdAt)(Ordering[java.time.Instant].reverse)
    )

  override def update(dashboard: Dashboard): F[Boolean] =
    state.modify { current =>
      if (current.contains(dashboard.id))
        (current.updated(dashboard.id, dashboard), true)
      else
        (current, false)
    }

  override def delete(id: DashboardId): F[Boolean] =
    state.modify { current =>
      if (current.contains(id))
        (current - id, true)
      else
        (current, false)
    }
}

object InMemoryDashboardRepo {

  def create[F[_]: Sync](dashboards: List[Dashboard] = Nil): F[InMemoryDashboardRepo[F]] =
    Ref
      .of[F, Map[DashboardId, Dashboard]](dashboards.map(dashboard =>
        dashboard.id -> dashboard
      ).toMap)
      .map(new InMemoryDashboardRepo[F](_))
}
