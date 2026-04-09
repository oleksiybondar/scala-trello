package io.github.oleksiybondar.api.infrastructure.db.timeTracking

import io.github.oleksiybondar.api.domain.timeTracking.{
  TimeTrackingDurationMinutes,
  TimeTrackingEntryDescription,
  TimeTrackingEntryId
}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.SlickTimeTrackingRepoFixtures.withCleanRepo
import io.github.oleksiybondar.api.testkit.fixtures.TimeTrackingFixtures
import munit.FunSuite

import java.time.Instant
import java.util.UUID

class SlickTimeTrackingRepoSpec extends FunSuite {

  test("create persists a time entry that can be loaded by id") {
    val entry = TimeTrackingFixtures.sampleEntry

    withCleanRepo { repo =>
      for {
        _      <- repo.create(entry)
        result <- repo.findById(entry.id)
      } yield assertEquals(result, Some(entry))
    }
  }

  test("listByTicket returns entries ordered by loggedAt descending") {
    val firstEntry  =
      TimeTrackingFixtures.entry(
        id = TimeTrackingEntryId(1),
        loggedAt = Instant.parse("2026-04-06T09:00:00Z")
      )
    val secondEntry =
      TimeTrackingFixtures.entry(
        id = TimeTrackingEntryId(2),
        loggedAt = Instant.parse("2026-04-06T11:00:00Z")
      )

    withCleanRepo { repo =>
      for {
        _      <- repo.create(firstEntry)
        _      <- repo.create(secondEntry)
        result <- repo.listByTicket(firstEntry.ticketId)
      } yield assertEquals(result, List(secondEntry, firstEntry))
    }
  }

  test("listByUser returns only entries for the requested user") {
    val ownedEntry = TimeTrackingFixtures.sampleEntry
    val otherEntry =
      TimeTrackingFixtures.entry(
        id = TimeTrackingEntryId(2),
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
      )

    withCleanRepo { repo =>
      for {
        _      <- repo.create(ownedEntry)
        _      <- repo.create(otherEntry)
        result <- repo.listByUser(ownedEntry.userId)
      } yield assertEquals(result, List(ownedEntry))
    }
  }

  test("update returns true and persists changes for an existing time entry") {
    val existingEntry = TimeTrackingFixtures.sampleEntry
    val updatedEntry  =
      existingEntry.copy(
        durationMinutes = TimeTrackingDurationMinutes(120),
        loggedAt = Instant.parse("2026-04-06T12:00:00Z"),
        description = Some(TimeTrackingEntryDescription("Updated worklog entry."))
      )

    withCleanRepo { repo =>
      for {
        _        <- repo.create(existingEntry)
        updated  <- repo.update(updatedEntry)
        reloaded <- repo.findById(existingEntry.id)
      } yield {
        assertEquals(updated, true)
        assertEquals(reloaded, Some(updatedEntry))
      }
    }
  }

  test("update returns false when the time entry does not exist") {
    withCleanRepo { repo =>
      repo.update(TimeTrackingFixtures.sampleEntry).map(updated => assertEquals(updated, false))
    }
  }

  test("delete returns true for an existing time entry and removes it") {
    val entry = TimeTrackingFixtures.sampleEntry

    withCleanRepo { repo =>
      for {
        _        <- repo.create(entry)
        deleted  <- repo.delete(entry.id)
        reloaded <- repo.findById(entry.id)
      } yield {
        assertEquals(deleted, true)
        assertEquals(reloaded, None)
      }
    }
  }

  test("delete returns false when the time entry does not exist") {
    withCleanRepo { repo =>
      repo.delete(TimeTrackingEntryId(999)).map(deleted => assertEquals(deleted, false))
    }
  }
}
