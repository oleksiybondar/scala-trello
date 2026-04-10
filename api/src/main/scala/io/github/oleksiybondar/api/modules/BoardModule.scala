package io.github.oleksiybondar.api.modules

import cats.effect.Async
import io.github.oleksiybondar.api.domain.board.{
  BoardAccessService,
  BoardAccessServiceLive,
  BoardMembershipService,
  BoardMembershipServiceLive,
  BoardService,
  BoardServiceLive
}
import io.github.oleksiybondar.api.domain.permission.RoleService
import io.github.oleksiybondar.api.infrastructure.db.board.{
  BoardMemberRepo,
  BoardQueryRepo,
  BoardRepo,
  SlickBoardMemberRepo,
  SlickBoardQueryRepo,
  SlickBoardRepo
}
import slick.jdbc.PostgresProfile.api.Database

final case class BoardModule[F[_]](
    boardRepo: BoardRepo[F],
    boardQueryRepo: BoardQueryRepo[F],
    boardMemberRepo: BoardMemberRepo[F],
    boardMembershipService: BoardMembershipService[F],
    boardAccessService: BoardAccessService[F],
    boardService: BoardService[F]
)

object BoardModule {

  def make[F[_]: Async](
      db: Database,
      roleService: RoleService[F]
  ): BoardModule[F] = {
    val boardRepo              = new SlickBoardRepo[F](db)
    val boardQueryRepo         = new SlickBoardQueryRepo[F](db)
    val boardMemberRepo        = new SlickBoardMemberRepo[F](db)
    val boardMembershipService = new BoardMembershipServiceLive[F](boardMemberRepo, roleService)
    val boardAccessService     = new BoardAccessServiceLive[F](boardRepo, boardMembershipService)

    BoardModule(
      boardRepo = boardRepo,
      boardQueryRepo = boardQueryRepo,
      boardMemberRepo = boardMemberRepo,
      boardMembershipService = boardMembershipService,
      boardAccessService = boardAccessService,
      boardService = new BoardServiceLive[F](
        boardRepo,
        boardAccessService,
        boardMembershipService,
        roleService
      )
    )
  }
}
