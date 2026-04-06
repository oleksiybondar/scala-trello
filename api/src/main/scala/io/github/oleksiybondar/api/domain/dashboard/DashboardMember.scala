package io.github.oleksiybondar.api.domain.dashboard

import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant

final case class DashboardMember(
    dashboardId: DashboardId,
    userId: UserId,
    roleId: RoleId,
    createdAt: Instant
)
