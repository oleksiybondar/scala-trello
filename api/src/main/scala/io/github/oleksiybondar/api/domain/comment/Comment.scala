package io.github.oleksiybondar.api.domain.comment

import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant

final case class CommentId(value: Long)        extends AnyVal
final case class CommentMessage(value: String) extends AnyVal

final case class Comment(
    id: CommentId,
    ticketId: TicketId,
    authorUserId: UserId,
    createdAt: Instant,
    modifiedAt: Instant,
    message: CommentMessage,
    relatedCommentId: Option[CommentId]
)
