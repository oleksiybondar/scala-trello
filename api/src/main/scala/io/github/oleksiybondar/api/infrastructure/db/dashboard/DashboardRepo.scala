package io.github.oleksiybondar.api.infrastructure.db.dashboard

import io.github.oleksiybondar.api.domain.dashboard.{Dashboard, DashboardId}
import io.github.oleksiybondar.api.domain.user.UserId

trait DashboardRepo[F[_]] {
  def create(dashboard: Dashboard): F[Unit]
  def findById(id: DashboardId): F[Option[Dashboard]]
  def list: F[List[Dashboard]]
  def listByOwner(ownerUserId: UserId): F[List[Dashboard]]
  def update(dashboard: Dashboard): F[Boolean]
  def delete(id: DashboardId): F[Boolean]
}
