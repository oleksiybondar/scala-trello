package io.github.oleksiybondar.api.modules

import cats.effect.Async
import io.github.oleksiybondar.api.config.AppConfig
import slick.jdbc.PostgresProfile.api.Database

final case class ApplicationModules[F[_]](
    user: UserModule[F],
    permission: PermissionModule[F],
    board: BoardModule[F],
    dictionary: DictionaryModule[F],
    ticket: TicketModule[F],
    timeTracking: TimeTrackingModule[F],
    comment: CommentModule[F],
    auth: AuthModule[F]
)

object ApplicationModules {

  def make[F[_]: Async](
      config: AppConfig,
      db: Database
  ): ApplicationModules[F] = {
    val userModule         = UserModule.make[F](config.password, db)
    val permissionModule   = PermissionModule.make[F](db)
    val boardModule        = BoardModule.make[F](db, permissionModule.roleService)
    val dictionaryModule   = DictionaryModule.make[F](db)
    val ticketModule       =
      TicketModule.make[F](db, boardModule.boardAccessService, boardModule.boardMembershipService)
    val timeTrackingModule =
      TimeTrackingModule.make[F](
        db,
        boardModule.boardRepo,
        ticketModule.ticketRepo,
        boardModule.boardAccessService,
        boardModule.boardMembershipService,
        dictionaryModule.timeTrackingActivityRepo
      )
    val commentModule      =
      CommentModule.make[F](
        db,
        ticketModule.ticketRepo,
        boardModule.boardRepo,
        boardModule.boardAccessService
      )
    val authModule         =
      AuthModule.make[F](
        config.auth,
        config.password,
        userModule.userRepo,
        db,
        userModule.passwordHistoryRepo
      )

    ApplicationModules(
      user = userModule,
      permission = permissionModule,
      board = boardModule,
      dictionary = dictionaryModule,
      ticket = ticketModule,
      timeTracking = timeTrackingModule,
      comment = commentModule,
      auth = authModule
    )
  }
}
