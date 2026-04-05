package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.dashboard.{
  Dashboard,
  DashboardDescription,
  DashboardId,
  DashboardName
}
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant
import java.util.UUID

object DashboardFixtures {

  val sampleDashboard: Dashboard =
    Dashboard(
      id = DashboardId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
      name = DashboardName("Core Board"),
      description = Some(DashboardDescription("Main project dashboard")),
      active = true,
      ownerUserId = UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
      createdByUserId = UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
      createdAt = Instant.parse("2026-04-05T08:00:00Z"),
      modifiedAt = Instant.parse("2026-04-05T08:00:00Z"),
      lastModifiedByUserId = UserId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
    )

  def dashboard(
      id: DashboardId = sampleDashboard.id,
      name: DashboardName = sampleDashboard.name,
      description: Option[DashboardDescription] = sampleDashboard.description,
      active: Boolean = sampleDashboard.active,
      ownerUserId: UserId = sampleDashboard.ownerUserId,
      createdByUserId: UserId = sampleDashboard.createdByUserId,
      createdAt: Instant = sampleDashboard.createdAt,
      modifiedAt: Instant = sampleDashboard.modifiedAt,
      lastModifiedByUserId: UserId = sampleDashboard.lastModifiedByUserId
  ): Dashboard =
    Dashboard(
      id = id,
      name = name,
      description = description,
      active = active,
      ownerUserId = ownerUserId,
      createdByUserId = createdByUserId,
      createdAt = createdAt,
      modifiedAt = modifiedAt,
      lastModifiedByUserId = lastModifiedByUserId
    )
}
