package io.github.oleksiybondar.api.infrastructure.db.ticket

import cats.effect.IO
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.testkit.fixtures.SlickTicketQueryRepoFixtures.withCleanRepoAndDatabase
import munit.FunSuite
import slick.jdbc.PostgresProfile.api._

class SlickTicketQueryRepoSpec extends FunSuite {

  test("findById returns ticket details with board, users, comment count, and time entries") {
    withCleanRepoAndDatabase { (db, repo) =>
      for {
        _      <- IO.fromFuture(
                    IO(
                      db.run(
                        DBIO.seq(
                          sqlu"""
                       INSERT INTO comments (id, ticket_id, author_user_id, created_at, modified_at, message, related_comment_id)
                       VALUES
                         (1, 1, '11111111-1111-1111-1111-111111111111', TIMESTAMPTZ '2026-04-06T12:00:00Z', TIMESTAMPTZ '2026-04-06T12:00:00Z', 'This needs a follow-up review.', NULL)
                     """,
                          sqlu"""
                       INSERT INTO time_tracking (id, ticket_id, user_id, activity_id, duration_minutes, logged_at, description)
                       VALUES
                         (1, 1, '11111111-1111-1111-1111-111111111111', 2, 90, TIMESTAMPTZ '2026-04-06T10:00:00Z', 'Implemented GraphQL ticket queries.')
                     """
                        )
                      )
                    )
                  ).void
        result <- repo.findById(TicketId(1))
      } yield {
        assert(result.nonEmpty)
        val ticket = result.getOrElse(fail("expected ticket details"))
        assertEquals(ticket.board.name, "Core Board")
        assertEquals(ticket.commentsCount, 1)
        assertEquals(ticket.createdBy.firstName, "Alice")
        assertEquals(ticket.assignedTo.map(_.firstName), Some("Bob"))
        assertEquals(ticket.timeEntries.length, 1)
        assertEquals(ticket.timeEntries.head.activityCode, "development")
        assertEquals(ticket.timeEntries.head.activityName, "Development")
        assertEquals(ticket.timeEntries.head.user.lastName, "Example")
      }
    }
  }

  test("findById returns None for an unknown ticket") {
    withCleanRepoAndDatabase { (_, repo) =>
      repo.findById(TicketId(999)).map(result => assertEquals(result, None))
    }
  }
}
