package io.github.oleksiybondar.api.infrastructure.db.comment

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.comment.CommentId
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.infrastructure.db.SharedSlickTables.{CommentsTable, UsersTable}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.util.UUID

final class SlickCommentQueryRepo[F[_]: Async](db: Database) extends CommentQueryRepo[F] {

  private final case class TicketRow(
      id: Long,
      boardId: UUID,
      name: String
  )

  private final class TicketsTable(tag: Tag) extends Table[TicketRow](tag, "tickets") {
    def id: Rep[Long]      = column[Long]("id", O.PrimaryKey)
    def boardId: Rep[UUID] = column[UUID]("dashboard_id")
    def name: Rep[String]  = column[String]("name")

    def * : ProvenShape[TicketRow] = (id, boardId, name).mapTo[TicketRow]
  }

  private val comments = TableQuery[CommentsTable]
  private val users    = TableQuery[UsersTable]
  private val tickets  = TableQuery[TicketsTable]

  override def listByTicket(ticketId: TicketId): F[List[CommentQueryRow]] =
    run(
      comments
        .filter(_.ticketId === ticketId.value)
        .join(users)
        .on(_.authorUserId === _.id)
        .join(tickets)
        .on(_._1.ticketId === _.id)
        .sortBy(_._1._1.createdAt.asc)
        .result
    ).map(_.toList.map { case ((comment, user), ticket) =>
      CommentQueryRow(
        id = CommentId(comment.id),
        ticketId = TicketId(comment.ticketId),
        authorUserId = comment.authorUserId.toString,
        createdAt = comment.createdAt.toString,
        modifiedAt = comment.modifiedAt.toString,
        message = comment.message,
        relatedCommentId = comment.relatedCommentId.map(_.toString),
        authorUsername = user.username,
        authorEmail = user.email,
        authorFirstName = user.firstName,
        authorLastName = user.lastName,
        authorAvatarUrl = user.avatarUrl,
        authorCreatedAt = user.createdAt.toString,
        ticketBoardId = ticket.boardId.toString,
        ticketTitle = ticket.name
      )
    })

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))
}
