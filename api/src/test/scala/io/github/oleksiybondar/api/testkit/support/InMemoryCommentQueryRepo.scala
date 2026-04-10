package io.github.oleksiybondar.api.testkit.support

import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.comment.CommentId
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.infrastructure.db.comment.{CommentQueryRepo, CommentQueryRow}

final class InMemoryCommentQueryRepo[F[_]: Sync](
    comments: List[io.github.oleksiybondar.api.domain.comment.Comment],
    tickets: List[io.github.oleksiybondar.api.domain.ticket.Ticket],
    users: List[io.github.oleksiybondar.api.domain.user.User]
) extends CommentQueryRepo[F] {

  override def listByTicket(ticketId: TicketId): F[List[CommentQueryRow]] =
    comments
      .filter(_.ticketId == ticketId)
      .sortBy(_.createdAt)
      .flatMap { comment =>
        for {
          ticket <- tickets.find(_.id == comment.ticketId)
          user   <- users.find(_.id == comment.authorUserId)
        } yield CommentQueryRow(
          id = CommentId(comment.id.value),
          ticketId = comment.ticketId,
          authorUserId = comment.authorUserId.value.toString,
          createdAt = comment.createdAt.toString,
          modifiedAt = comment.modifiedAt.toString,
          message = comment.message.value,
          relatedCommentId = comment.relatedCommentId.map(_.value.toString),
          authorUsername = user.username.map(_.value),
          authorEmail = user.email.map(_.value),
          authorFirstName = user.firstName.value,
          authorLastName = user.lastName.value,
          authorAvatarUrl = user.avatarUrl.map(_.value),
          authorCreatedAt = user.createdAt.toString,
          ticketBoardId = ticket.boardId.value.toString,
          ticketTitle = ticket.name.value
        )
      }
      .pure[F]
}
