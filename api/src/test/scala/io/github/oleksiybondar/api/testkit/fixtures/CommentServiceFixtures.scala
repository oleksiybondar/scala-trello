package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.board.{Board, BoardAccessServiceLive, BoardMember}
import io.github.oleksiybondar.api.domain.comment.{Comment, CommentServiceLive}
import io.github.oleksiybondar.api.domain.permission.{Permission, Role}
import io.github.oleksiybondar.api.domain.ticket.Ticket
import io.github.oleksiybondar.api.testkit.support.{
  InMemoryBoardRepo,
  InMemoryCommentRepo,
  InMemoryTicketRepo
}

object CommentServiceFixtures {

  final case class CommentServiceContext(
      boardRepo: InMemoryBoardRepo[IO],
      ticketRepo: InMemoryTicketRepo[IO],
      commentRepo: InMemoryCommentRepo[IO],
      membershipFixtures: BoardMembershipServiceFixtures.BoardMembershipServiceContext,
      commentService: CommentServiceLive[IO]
  )

  def withCommentService[A](
      comments: List[Comment] = Nil,
      tickets: List[Ticket] = Nil,
      boards: List[Board] = Nil,
      members: List[BoardMember] = Nil,
      roles: List[Role] = Nil,
      permissions: List[Permission] = Nil
  )(run: CommentServiceContext => IO[A]): A =
    (
      for {
        boardRepo   <- InMemoryBoardRepo.create[IO](boards)
        ticketRepo  <- InMemoryTicketRepo.create[IO](tickets)
        commentRepo <- InMemoryCommentRepo.create[IO](comments)
        result      <- IO.pure(
                         BoardMembershipServiceFixtures.withBoardMembershipService(
                           members = members,
                           roles = roles,
                           permissions = permissions
                         ) { membershipCtx =>
                           run(
                             CommentServiceContext(
                               boardRepo = boardRepo,
                               ticketRepo = ticketRepo,
                               commentRepo = commentRepo,
                               membershipFixtures = membershipCtx,
                               commentService = new CommentServiceLive[IO](
                                 commentRepo,
                                 ticketRepo,
                                 boardRepo,
                                 new BoardAccessServiceLive[IO](
                                   boardRepo,
                                   membershipCtx.dashboardMembershipService
                                 )
                               )
                             )
                           )
                         }
                       )
      } yield result
    ).unsafeRunSync()
}
