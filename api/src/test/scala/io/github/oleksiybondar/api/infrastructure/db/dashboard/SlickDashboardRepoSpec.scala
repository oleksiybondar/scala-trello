package io.github.oleksiybondar.api.infrastructure.db.dashboard

import io.github.oleksiybondar.api.domain.dashboard.{
  DashboardDescription,
  DashboardId,
  DashboardName
}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.DashboardFixtures
import io.github.oleksiybondar.api.testkit.fixtures.SlickDashboardRepoFixtures.withCleanRepo
import munit.FunSuite

import java.time.Instant
import java.util.UUID

class SlickDashboardRepoSpec extends FunSuite {

  test("create persists a dashboard that can be loaded by id") {
    val dashboard = DashboardFixtures.sampleDashboard

    withCleanRepo { repo =>
      for {
        _      <- repo.create(dashboard)
        result <- repo.findById(dashboard.id)
      } yield assertEquals(result, Some(dashboard))
    }
  }

  test("list returns persisted dashboards ordered by createdAt descending") {
    val firstDashboard  =
      DashboardFixtures.dashboard(
        id = DashboardId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")),
        name = DashboardName("First"),
        createdAt = Instant.parse("2026-04-05T09:00:00Z"),
        modifiedAt = Instant.parse("2026-04-05T09:00:00Z")
      )
    val secondDashboard =
      DashboardFixtures.dashboard(
        id = DashboardId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")),
        name = DashboardName("Second"),
        createdAt = Instant.parse("2026-04-05T10:00:00Z"),
        modifiedAt = Instant.parse("2026-04-05T10:00:00Z")
      )

    withCleanRepo { repo =>
      for {
        _      <- repo.create(firstDashboard)
        _      <- repo.create(secondDashboard)
        result <- repo.list
      } yield assertEquals(result, List(secondDashboard, firstDashboard))
    }
  }

  test("listByOwner returns only dashboards for the requested owner") {
    val ownedDashboard = DashboardFixtures.sampleDashboard
    val otherDashboard =
      DashboardFixtures.dashboard(
        id = DashboardId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")),
        name = DashboardName("Other"),
        ownerUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        createdByUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        lastModifiedByUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
      )

    withCleanRepo { repo =>
      for {
        _      <- repo.create(ownedDashboard)
        _      <- repo.create(otherDashboard)
        result <- repo.listByOwner(ownedDashboard.ownerUserId)
      } yield assertEquals(result, List(ownedDashboard))
    }
  }

  test("update returns true and persists changes for an existing dashboard") {
    val existingDashboard = DashboardFixtures.sampleDashboard
    val updatedDashboard  =
      existingDashboard.copy(
        name = DashboardName("Updated Board"),
        description = Some(DashboardDescription("Updated description")),
        active = false,
        modifiedAt = Instant.parse("2026-04-05T11:00:00Z")
      )

    withCleanRepo { repo =>
      for {
        _        <- repo.create(existingDashboard)
        updated  <- repo.update(updatedDashboard)
        reloaded <- repo.findById(existingDashboard.id)
      } yield {
        assertEquals(updated, true)
        assertEquals(reloaded, Some(updatedDashboard))
      }
    }
  }

  test("update returns false when the dashboard does not exist") {
    withCleanRepo { repo =>
      repo.update(DashboardFixtures.sampleDashboard).map { updated =>
        assertEquals(updated, false)
      }
    }
  }

  test("delete returns true for an existing dashboard and removes it") {
    val dashboard = DashboardFixtures.sampleDashboard

    withCleanRepo { repo =>
      for {
        _        <- repo.create(dashboard)
        deleted  <- repo.delete(dashboard.id)
        reloaded <- repo.findById(dashboard.id)
      } yield {
        assertEquals(deleted, true)
        assertEquals(reloaded, None)
      }
    }
  }

  test("delete returns false when the dashboard does not exist") {
    withCleanRepo { repo =>
      repo
        .delete(DashboardId(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")))
        .map { deleted =>
          assertEquals(deleted, false)
        }
    }
  }
}
