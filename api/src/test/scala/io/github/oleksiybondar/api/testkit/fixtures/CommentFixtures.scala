package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.comment.{Comment, CommentId, CommentMessage}
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant
import java.util.UUID

object CommentFixtures {

  val sampleComment: Comment =
    Comment(
      id = CommentId(1),
      ticketId = TicketFixtures.sampleTicket.id,
      authorUserId = UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
      createdAt = Instant.parse("2026-04-06T12:00:00Z"),
      modifiedAt = Instant.parse("2026-04-06T12:00:00Z"),
      message = CommentMessage("This needs a follow-up review."),
      relatedCommentId = None
    )

  def comment(
      id: CommentId = sampleComment.id,
      ticketId: TicketId = sampleComment.ticketId,
      authorUserId: UserId = sampleComment.authorUserId,
      createdAt: Instant = sampleComment.createdAt,
      modifiedAt: Instant = sampleComment.modifiedAt,
      message: CommentMessage = sampleComment.message,
      relatedCommentId: Option[CommentId] = sampleComment.relatedCommentId
  ): Comment =
    Comment(
      id = id,
      ticketId = ticketId,
      authorUserId = authorUserId,
      createdAt = createdAt,
      modifiedAt = modifiedAt,
      message = message,
      relatedCommentId = relatedCommentId
    )
}
