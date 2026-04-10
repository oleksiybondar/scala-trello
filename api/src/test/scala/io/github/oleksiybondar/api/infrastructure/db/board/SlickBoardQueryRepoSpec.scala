package io.github.oleksiybondar.api.infrastructure.db.board

import io.github.oleksiybondar.api.domain.board.BoardId
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.SlickBoardQueryRepoFixtures.withCleanRepo
import munit.FunSuite

import java.util.UUID

class SlickBoardQueryRepoSpec extends FunSuite {

  private val boardId = BoardId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
  private val aliceId = UserId(UUID.fromString("11111111-1111-1111-1111-111111111111"))

  test(
    "findById returns board details with owner, current user role permissions, and ticket tracked time"
  ) {
    withCleanRepo { repo =>
      repo.findById(boardId, aliceId).map { result =>
        assert(result.nonEmpty)
        val board = result.getOrElse(fail("expected board details"))
        assertEquals(board.name, "Core Board")
        assertEquals(board.owner.firstName, "Alice")
        assertEquals(board.createdBy.lastName, "Example")
        assertEquals(board.membersCount, 2)
        assertEquals(board.currentUserRole.map(_.name), Some("admin"))
        assertEquals(board.currentUserRole.map(_.permissions.size), Some(3))
        assertEquals(board.tickets.length, 1)
        assertEquals(board.tickets.head.name, "Implement login mutation")
        assertEquals(board.tickets.head.estimatedMinutes, Some(120))
        assertEquals(board.tickets.head.trackedMinutes, 90)
      }
    }
  }

  test("findById returns None for an unknown board") {
    val missingBoardId = BoardId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))

    withCleanRepo { repo =>
      repo.findById(missingBoardId, aliceId).map(result => assertEquals(result, None))
    }
  }
}
