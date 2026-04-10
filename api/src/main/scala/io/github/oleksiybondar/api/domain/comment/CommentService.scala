package io.github.oleksiybondar.api.domain.comment

import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.user.UserId

final case class CreateCommentCommand(
    ticketId: TicketId,
    message: CommentMessage,
    relatedCommentId: Option[CommentId]
)

trait CommentService[F[_]] {
  def createComment(command: CreateCommentCommand, actorUserId: UserId): F[Option[Comment]]
  def getComment(id: CommentId): F[Option[Comment]]
  def listComments(ticketId: TicketId, actorUserId: UserId): F[List[Comment]]
  def listCommentsByUser(userId: UserId, actorUserId: UserId): F[List[Comment]]
  def changeMessage(id: CommentId, actorUserId: UserId, message: CommentMessage): F[Boolean]
  def deleteComment(id: CommentId, actorUserId: UserId): F[Boolean]
}
