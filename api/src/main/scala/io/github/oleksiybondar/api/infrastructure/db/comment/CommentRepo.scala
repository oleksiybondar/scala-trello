package io.github.oleksiybondar.api.infrastructure.db.comment

import io.github.oleksiybondar.api.domain.comment.{Comment, CommentId}
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.user.UserId

trait CommentRepo[F[_]] {
  def nextId: F[CommentId]
  def create(comment: Comment): F[Unit]
  def findById(id: CommentId): F[Option[Comment]]
  def listByTicket(ticketId: TicketId): F[List[Comment]]
  def listByAuthor(userId: UserId): F[List[Comment]]
  def update(comment: Comment): F[Boolean]
  def delete(id: CommentId): F[Boolean]
}
