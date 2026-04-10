package io.github.oleksiybondar.api.infrastructure.db.comment

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.comment.{Comment, CommentId, CommentMessage}
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.SharedSlickTables.{CommentRow, CommentsTable}
import slick.jdbc.PostgresProfile.api._

final class SlickCommentRepo[F[_]: Async](db: Database) extends CommentRepo[F] {

  private val comments = TableQuery[CommentsTable]

  override def nextId: F[CommentId] =
    run(comments.map(_.id).max.result).map(id => CommentId(id.getOrElse(0L) + 1L))

  override def create(comment: Comment): F[Unit] =
    run(comments += toRow(comment)).void

  override def findById(id: CommentId): F[Option[Comment]] =
    run(
      comments
        .filter(_.id === id.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def listByTicket(ticketId: TicketId): F[List[Comment]] =
    run(
      comments
        .filter(_.ticketId === ticketId.value)
        .sortBy(_.createdAt.asc)
        .result
    ).map(_.toList.map(toDomain))

  override def listByAuthor(userId: UserId): F[List[Comment]] =
    run(
      comments
        .filter(_.authorUserId === userId.value)
        .sortBy(_.createdAt.desc)
        .result
    ).map(_.toList.map(toDomain))

  override def update(comment: Comment): F[Boolean] =
    run(
      comments
        .filter(_.id === comment.id.value)
        .update(toRow(comment))
    ).map(_ > 0)

  override def delete(id: CommentId): F[Boolean] =
    run(
      comments
        .filter(_.id === id.value)
        .delete
    ).map(_ > 0)

  private def toRow(comment: Comment): CommentRow =
    CommentRow(
      id = comment.id.value,
      ticketId = comment.ticketId.value,
      authorUserId = comment.authorUserId.value,
      createdAt = comment.createdAt,
      modifiedAt = comment.modifiedAt,
      message = comment.message.value,
      relatedCommentId = comment.relatedCommentId.map(_.value)
    )

  private def toDomain(row: CommentRow): Comment =
    Comment(
      id = CommentId(row.id),
      ticketId = TicketId(row.ticketId),
      authorUserId = UserId(row.authorUserId),
      createdAt = row.createdAt,
      modifiedAt = row.modifiedAt,
      message = CommentMessage(row.message),
      relatedCommentId = row.relatedCommentId.map(CommentId(_))
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))
}
