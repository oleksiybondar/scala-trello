package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.comment.{Comment, CommentId}
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.comment.CommentRepo

final class InMemoryCommentRepo[F[_]: Sync] private (
    state: Ref[F, Map[CommentId, Comment]]
) extends CommentRepo[F] {

  override def nextId: F[CommentId] =
    state.get.map(current => CommentId(current.keys.map(_.value).maxOption.getOrElse(0L) + 1L))

  override def create(comment: Comment): F[Unit] =
    state.update(_.updated(comment.id, comment))

  override def findById(id: CommentId): F[Option[Comment]] =
    state.get.map(_.get(id))

  override def listByTicket(ticketId: TicketId): F[List[Comment]] =
    state.get.map(
      _.values.toList
        .filter(_.ticketId == ticketId)
        .sortBy(_.createdAt)
    )

  override def listByAuthor(userId: UserId): F[List[Comment]] =
    state.get.map(
      _.values.toList
        .filter(_.authorUserId == userId)
        .sortBy(_.createdAt)(Ordering[java.time.Instant].reverse)
    )

  override def update(comment: Comment): F[Boolean] =
    state.modify { current =>
      if (current.contains(comment.id)) (current.updated(comment.id, comment), true)
      else (current, false)
    }

  override def delete(id: CommentId): F[Boolean] =
    state.modify { current =>
      if (current.contains(id)) (current - id, true)
      else (current, false)
    }
}

object InMemoryCommentRepo {

  def create[F[_]: Sync](comments: List[Comment] = Nil): F[InMemoryCommentRepo[F]] =
    Ref.of[F, Map[CommentId, Comment]](comments.map(comment => comment.id -> comment).toMap)
      .map(new InMemoryCommentRepo[F](_))
}
