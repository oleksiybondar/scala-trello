package io.github.oleksiybondar.api.infrastructure.db.comment

import io.github.oleksiybondar.api.domain.comment.CommentId
import io.github.oleksiybondar.api.domain.ticket.TicketId

final case class CommentQueryRow(
    id: CommentId,
    ticketId: TicketId,
    authorUserId: String,
    createdAt: String,
    modifiedAt: String,
    message: String,
    relatedCommentId: Option[String],
    authorUsername: Option[String],
    authorEmail: Option[String],
    authorFirstName: String,
    authorLastName: String,
    authorAvatarUrl: Option[String],
    authorCreatedAt: String,
    ticketBoardId: String,
    ticketTitle: String
)

trait CommentQueryRepo[F[_]] {
  def listByTicket(ticketId: TicketId): F[List[CommentQueryRow]]
}
