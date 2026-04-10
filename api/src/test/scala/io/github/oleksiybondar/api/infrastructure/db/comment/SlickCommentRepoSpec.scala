package io.github.oleksiybondar.api.infrastructure.db.comment

import io.github.oleksiybondar.api.domain.comment.{CommentId, CommentMessage}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.CommentFixtures
import io.github.oleksiybondar.api.testkit.fixtures.SlickCommentRepoFixtures.withCleanRepo
import munit.FunSuite

import java.time.Instant
import java.util.UUID

class SlickCommentRepoSpec extends FunSuite {

  test("create persists a comment that can be loaded by id") {
    val comment = CommentFixtures.sampleComment

    withCleanRepo { repo =>
      for {
        _      <- repo.create(comment)
        result <- repo.findById(comment.id)
      } yield assertEquals(result, Some(comment))
    }
  }

  test("listByTicket returns comments ordered by createdAt ascending") {
    val firstComment  =
      CommentFixtures.comment(
        id = CommentId(1),
        createdAt = Instant.parse("2026-04-06T09:00:00Z"),
        modifiedAt = Instant.parse("2026-04-06T09:00:00Z")
      )
    val secondComment =
      CommentFixtures.comment(
        id = CommentId(2),
        createdAt = Instant.parse("2026-04-06T10:00:00Z"),
        modifiedAt = Instant.parse("2026-04-06T10:00:00Z"),
        message = CommentMessage("Second comment")
      )

    withCleanRepo { repo =>
      for {
        _      <- repo.create(secondComment)
        _      <- repo.create(firstComment)
        result <- repo.listByTicket(firstComment.ticketId)
      } yield assertEquals(result, List(firstComment, secondComment))
    }
  }

  test("update returns true and persists changes for an existing comment") {
    val existingComment = CommentFixtures.sampleComment
    val updatedComment  =
      existingComment.copy(
        modifiedAt = Instant.parse("2026-04-06T13:00:00Z"),
        message = CommentMessage("Updated comment message")
      )

    withCleanRepo { repo =>
      for {
        _        <- repo.create(existingComment)
        updated  <- repo.update(updatedComment)
        reloaded <- repo.findById(existingComment.id)
      } yield {
        assertEquals(updated, true)
        assertEquals(reloaded, Some(updatedComment))
      }
    }
  }

  test("listByAuthor returns comments ordered by createdAt descending") {
    val firstComment       =
      CommentFixtures.comment(
        id = CommentId(1),
        createdAt = Instant.parse("2026-04-06T09:00:00Z"),
        modifiedAt = Instant.parse("2026-04-06T09:00:00Z")
      )
    val secondComment      =
      CommentFixtures.comment(
        id = CommentId(2),
        createdAt = Instant.parse("2026-04-06T10:00:00Z"),
        modifiedAt = Instant.parse("2026-04-06T10:00:00Z"),
        message = CommentMessage("Second by same author")
      )
    val otherAuthorComment =
      CommentFixtures.comment(
        id = CommentId(3),
        authorUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
      )

    withCleanRepo { repo =>
      for {
        _      <- repo.create(firstComment)
        _      <- repo.create(secondComment)
        _      <- repo.create(otherAuthorComment)
        result <- repo.listByAuthor(firstComment.authorUserId)
      } yield assertEquals(result, List(secondComment, firstComment))
    }
  }

  test("update returns false when the comment does not exist") {
    withCleanRepo { repo =>
      repo.update(CommentFixtures.sampleComment).map(updated => assertEquals(updated, false))
    }
  }

  test("delete returns true for an existing comment and removes it") {
    val comment = CommentFixtures.sampleComment

    withCleanRepo { repo =>
      for {
        _        <- repo.create(comment)
        deleted  <- repo.delete(comment.id)
        reloaded <- repo.findById(comment.id)
      } yield {
        assertEquals(deleted, true)
        assertEquals(reloaded, None)
      }
    }
  }

  test("delete returns false when the comment does not exist") {
    withCleanRepo { repo =>
      repo.delete(CommentId(999)).map(deleted => assertEquals(deleted, false))
    }
  }
}
