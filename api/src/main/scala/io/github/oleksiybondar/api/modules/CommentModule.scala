package io.github.oleksiybondar.api.modules

import cats.effect.Async
import io.github.oleksiybondar.api.domain.board.BoardAccessService
import io.github.oleksiybondar.api.domain.comment.{CommentService, CommentServiceLive}
import io.github.oleksiybondar.api.infrastructure.db.board.BoardRepo
import io.github.oleksiybondar.api.infrastructure.db.comment.{CommentRepo, SlickCommentRepo}
import io.github.oleksiybondar.api.infrastructure.db.ticket.TicketRepo
import slick.jdbc.PostgresProfile.api.Database

final case class CommentModule[F[_]](
    commentRepo: CommentRepo[F],
    commentService: CommentService[F]
)

object CommentModule {

  def make[F[_]: Async](
      db: Database,
      ticketRepo: TicketRepo[F],
      boardRepo: BoardRepo[F],
      boardAccessService: BoardAccessService[F]
  ): CommentModule[F] = {
    val commentRepo = new SlickCommentRepo[F](db)

    CommentModule(
      commentRepo = commentRepo,
      commentService = new CommentServiceLive[F](
        commentRepo,
        ticketRepo,
        boardRepo,
        boardAccessService
      )
    )
  }
}
