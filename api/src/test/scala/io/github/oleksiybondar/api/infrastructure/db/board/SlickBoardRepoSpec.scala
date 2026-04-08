package io.github.oleksiybondar.api.infrastructure.db.board

import cats.effect.IO
import io.github.oleksiybondar.api.domain.board.{BoardDescription, BoardId, BoardName}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.BoardFixtures
import io.github.oleksiybondar.api.testkit.fixtures.SlickBoardRepoFixtures.{
  withCleanRepo,
  withCleanRepoAndDatabase
}
import munit.FunSuite
import slick.jdbc.PostgresProfile.api._

import java.time.Instant
import java.util.UUID

class SlickBoardRepoSpec extends FunSuite {

  test("create persists a dashboard that can be loaded by id") {
    val dashboard = BoardFixtures.sampleDashboard

    withCleanRepo { repo =>
      for {
        _      <- repo.create(dashboard)
        result <- repo.findById(dashboard.id)
      } yield assertEquals(result, Some(dashboard))
    }
  }

  test("list returns persisted dashboards ordered by createdAt descending") {
    val firstDashboard  =
      BoardFixtures.dashboard(
        id = BoardId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")),
        name = BoardName("First"),
        createdAt = Instant.parse("2026-04-05T09:00:00Z"),
        modifiedAt = Instant.parse("2026-04-05T09:00:00Z")
      )
    val secondDashboard =
      BoardFixtures.dashboard(
        id = BoardId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")),
        name = BoardName("Second"),
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
    val ownedDashboard = BoardFixtures.sampleDashboard
    val otherDashboard =
      BoardFixtures.dashboard(
        id = BoardId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")),
        name = BoardName("Other"),
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

  test("listByMember returns dashboards where the user has membership") {
    val firstDashboard  = BoardFixtures.sampleDashboard
    val secondDashboard =
      BoardFixtures.dashboard(
        id = BoardId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")),
        name = BoardName("Membership Board"),
        ownerUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        createdByUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        lastModifiedByUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        createdAt = Instant.parse("2026-04-05T12:00:00Z"),
        modifiedAt = Instant.parse("2026-04-05T12:00:00Z")
      )

    withCleanRepoAndDatabase { (db, repo) =>
      for {
        _      <- repo.create(firstDashboard)
        _      <- repo.create(secondDashboard)
        _      <- IO.fromFuture(
                    IO(
                      db.run(
                        DBIO.seq(
                          sqlu"""
                            INSERT INTO roles (id, name, description)
                            VALUES (1, 'admin', 'Full dashboard access including member management.')
                          """,
                          sqlu"""
                            INSERT INTO dashboard_members (dashboard_id, user_id, role_id, created_at)
                            VALUES
                              ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 1, TIMESTAMPTZ '2026-04-05T08:00:00Z'),
                              ('ffffffff-ffff-ffff-ffff-ffffffffffff', '22222222-2222-2222-2222-222222222222', 1, TIMESTAMPTZ '2026-04-05T12:00:00Z')
                          """
                        )
                      )
                    )
                  ).void
        result <- repo.listByMember(UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")))
      } yield assertEquals(result, List(firstDashboard))
    }
  }

  test("update returns true and persists changes for an existing dashboard") {
    val existingDashboard = BoardFixtures.sampleDashboard
    val updatedDashboard  =
      existingDashboard.copy(
        name = BoardName("Updated Board"),
        description = Some(BoardDescription("Updated description")),
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
      repo.update(BoardFixtures.sampleDashboard).map { updated =>
        assertEquals(updated, false)
      }
    }
  }

  test("delete returns true for an existing dashboard and removes it") {
    val dashboard = BoardFixtures.sampleDashboard

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
        .delete(BoardId(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")))
        .map { deleted =>
          assertEquals(deleted, false)
        }
    }
  }
}
