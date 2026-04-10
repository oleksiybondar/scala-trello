package io.github.oleksiybondar.api.infrastructure.db.ticket

import io.github.oleksiybondar.api.domain.ticket.{TicketId, TicketName}
import io.github.oleksiybondar.api.testkit.fixtures.SlickTicketRepoFixtures.withCleanRepo
import io.github.oleksiybondar.api.testkit.fixtures.TicketFixtures
import munit.FunSuite

import java.time.Instant

class SlickTicketRepoSpec extends FunSuite {

  test("create persists a ticket that can be loaded by id") {
    val ticket = TicketFixtures.sampleTicket

    withCleanRepo { repo =>
      for {
        _      <- repo.create(ticket)
        result <- repo.findById(ticket.id)
      } yield assertEquals(result, Some(ticket))
    }
  }

  test("listByBoard returns tickets ordered by createdAt descending") {
    val firstTicket  =
      TicketFixtures.ticket(
        id = TicketId(1),
        name = TicketName("First"),
        createdAt = Instant.parse("2026-04-05T09:00:00Z"),
        modifiedAt = Instant.parse("2026-04-05T09:00:00Z")
      )
    val secondTicket =
      TicketFixtures.ticket(
        id = TicketId(2),
        name = TicketName("Second"),
        createdAt = Instant.parse("2026-04-05T10:00:00Z"),
        modifiedAt = Instant.parse("2026-04-05T10:00:00Z")
      )

    withCleanRepo { repo =>
      for {
        _      <- repo.create(firstTicket)
        _      <- repo.create(secondTicket)
        result <- repo.listByBoard(firstTicket.boardId)
      } yield assertEquals(result, List(secondTicket, firstTicket))
    }
  }

  test("update returns true and persists changes for an existing ticket") {
    val existingTicket = TicketFixtures.sampleTicket
    val updatedTicket  =
      existingTicket.copy(
        name = TicketName("Updated ticket"),
        modifiedAt = Instant.parse("2026-04-05T11:00:00Z"),
        originalEstimatedMinutes = Some(180),
        commentsEnabled = false
      )

    withCleanRepo { repo =>
      for {
        _        <- repo.create(existingTicket)
        updated  <- repo.update(updatedTicket)
        reloaded <- repo.findById(existingTicket.id)
      } yield {
        assertEquals(updated, true)
        assertEquals(reloaded, Some(updatedTicket))
      }
    }
  }

  test("update returns false when the ticket does not exist") {
    withCleanRepo { repo =>
      repo.update(TicketFixtures.sampleTicket).map(updated => assertEquals(updated, false))
    }
  }

  test("delete returns true for an existing ticket and removes it") {
    val ticket = TicketFixtures.sampleTicket

    withCleanRepo { repo =>
      for {
        _        <- repo.create(ticket)
        deleted  <- repo.delete(ticket.id)
        reloaded <- repo.findById(ticket.id)
      } yield {
        assertEquals(deleted, true)
        assertEquals(reloaded, None)
      }
    }
  }

  test("delete returns false when the ticket does not exist") {
    withCleanRepo { repo =>
      repo
        .delete(TicketId(999))
        .map(deleted => assertEquals(deleted, false))
    }
  }

  test("listByBoard returns only tickets for the requested board") {
    val ownedTicket = TicketFixtures.sampleTicket

    withCleanRepo { repo =>
      for {
        _      <- repo.create(ownedTicket)
        result <- repo.listByBoard(ownedTicket.boardId)
      } yield assertEquals(result, List(ownedTicket))
    }
  }
}
