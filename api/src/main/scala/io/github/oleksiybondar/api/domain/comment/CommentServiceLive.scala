package io.github.oleksiybondar.api.domain.comment

import cats.effect.kernel.Temporal
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.BoardAccessService
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.board.BoardRepo
import io.github.oleksiybondar.api.infrastructure.db.comment.CommentRepo
import io.github.oleksiybondar.api.infrastructure.db.ticket.TicketRepo

final class CommentServiceLive[F[_]: Temporal](
    commentRepo: CommentRepo[F],
    ticketRepo: TicketRepo[F],
    boardRepo: BoardRepo[F],
    boardAccessService: BoardAccessService[F]
) extends CommentService[F] {

  override def createComment(
      command: CreateCommentCommand,
      actorUserId: UserId
  ): F[Option[Comment]] =
    canWriteComment(
      command.ticketId,
      actorUserId,
      (accessService, boardId, userId) => accessService.canCreateComment(boardId, userId)
    ).flatMap {
      case false => none[Comment].pure[F]
      case true  =>
        command.relatedCommentId match {
          case Some(relatedCommentId) =>
            commentRepo.findById(relatedCommentId).flatMap {
              case Some(related) if related.ticketId == command.ticketId =>
                persistComment(command, actorUserId)
              case _                                                     => none[Comment].pure[F]
            }
          case None                   => persistComment(command, actorUserId)
        }
    }

  override def getComment(id: CommentId): F[Option[Comment]] =
    commentRepo.findById(id)

  override def listComments(
      ticketId: io.github.oleksiybondar.api.domain.ticket.TicketId,
      actorUserId: UserId
  ): F[List[Comment]] =
    ticketRepo.findById(ticketId).flatMap {
      case None         => List.empty[Comment].pure[F]
      case Some(ticket) =>
        boardAccessService.canReadComment(ticket.boardId, actorUserId).flatMap {
          case true  => commentRepo.listByTicket(ticketId)
          case false => List.empty[Comment].pure[F]
        }
    }

  override def listCommentsByUser(userId: UserId, actorUserId: UserId): F[List[Comment]] =
    if (userId == actorUserId) commentRepo.listByAuthor(userId)
    else List.empty[Comment].pure[F]

  override def changeMessage(
      id: CommentId,
      actorUserId: UserId,
      message: CommentMessage
  ): F[Boolean] =
    commentRepo.findById(id).flatMap {
      case Some(existing) if existing.authorUserId == actorUserId =>
        canWriteComment(
          existing.ticketId,
          actorUserId,
          (accessService, boardId, userId) => accessService.canModifyComment(boardId, userId)
        ).flatMap {
          case false => false.pure[F]
          case true  =>
            Temporal[F].realTimeInstant.flatMap { now =>
              commentRepo.update(existing.copy(message = message, modifiedAt = now))
            }
        }
      case _                                                      => false.pure[F]
    }

  override def deleteComment(id: CommentId, actorUserId: UserId): F[Boolean] =
    commentRepo.findById(id).flatMap {
      case Some(existing) if existing.authorUserId == actorUserId =>
        canWriteComment(
          existing.ticketId,
          actorUserId,
          (accessService, boardId, userId) => accessService.canDeleteComment(boardId, userId)
        ).flatMap {
          case false => false.pure[F]
          case true  => commentRepo.delete(id)
        }
      case _                                                      => false.pure[F]
    }

  private def persistComment(
      command: CreateCommentCommand,
      actorUserId: UserId
  ): F[Option[Comment]] =
    for {
      id     <- commentRepo.nextId
      now    <- Temporal[F].realTimeInstant
      comment = Comment(
                  id = id,
                  ticketId = command.ticketId,
                  authorUserId = actorUserId,
                  createdAt = now,
                  modifiedAt = now,
                  message = command.message,
                  relatedCommentId = command.relatedCommentId
                )
      _      <- commentRepo.create(comment)
    } yield Some(comment)

  private def canWriteComment(
      ticketId: io.github.oleksiybondar.api.domain.ticket.TicketId,
      actorUserId: UserId,
      permissionCheck: (
          BoardAccessService[F],
          io.github.oleksiybondar.api.domain.board.BoardId,
          UserId
      ) => F[Boolean]
  ): F[Boolean] =
    ticketRepo.findById(ticketId).flatMap {
      case None         => false.pure[F]
      case Some(ticket) =>
        boardRepo.findById(ticket.boardId).flatMap {
          case Some(board) if board.active && ticket.commentsEnabled =>
            permissionCheck(boardAccessService, ticket.boardId, actorUserId)
          case _                                                     => false.pure[F]
        }
    }
}
