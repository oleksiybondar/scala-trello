package io.github.oleksiybondar.api.domain.dashboard

import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant
import java.util.UUID

final case class DashboardId(value: UUID)            extends AnyVal
final case class DashboardName(value: String)        extends AnyVal
final case class DashboardDescription(value: String) extends AnyVal

final case class Dashboard(
    id: DashboardId,
    name: DashboardName,
    description: Option[DashboardDescription],
    active: Boolean,
    ownerUserId: UserId,
    createdByUserId: UserId,
    createdAt: Instant,
    modifiedAt: Instant,
    lastModifiedByUserId: UserId
)
