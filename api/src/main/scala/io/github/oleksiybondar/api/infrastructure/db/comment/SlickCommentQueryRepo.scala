package io.github.oleksiybondar.api.infrastructure.db.comment

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.comment.CommentId
import io.github.oleksiybondar.api.domain.ticket.TicketId
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

final class SlickCommentQueryRepo[F[_]: Async](db: Database) extends CommentQueryRepo[F] {

  private final case class CommentRow(
      id: Long,
      ticketId: Long,
      authorUserId: UUID,
      createdAt: Instant,
      modifiedAt: Instant,
      message: String,
      relatedCommentId: Option[Long]
  )

  private final case class UserRow(
      id: UUID,
      username: Option[String],
      email: Option[String],
      passwordHash: String,
      firstName: String,
      lastName: String,
      avatarUrl: Option[String],
      createdAt: Instant
  )

  private final case class TicketRow(
      id: Long,
      boardId: UUID,
      name: String
  )

  private final class CommentsTable(tag: Tag) extends Table[CommentRow](tag, "comments") {
    def id: Rep[Long]                       = column[Long]("id", O.PrimaryKey)
    def ticketId: Rep[Long]                 = column[Long]("ticket_id")
    def authorUserId: Rep[UUID]             = column[UUID]("author_user_id")
    def createdAt: Rep[Instant]             = column[Instant]("created_at")
    def modifiedAt: Rep[Instant]            = column[Instant]("modified_at")
    def message: Rep[String]                = column[String]("message")
    def relatedCommentId: Rep[Option[Long]] = column[Option[Long]]("related_comment_id")

    def * : ProvenShape[CommentRow] = (
      id,
      ticketId,
      authorUserId,
      createdAt,
      modifiedAt,
      message,
      relatedCommentId
    ).mapTo[CommentRow]
  }

  private final class UsersTable(tag: Tag) extends Table[UserRow](tag, "users") {
    def id: Rep[UUID]                  = column[UUID]("id", O.PrimaryKey)
    def username: Rep[Option[String]]  = column[Option[String]]("username")
    def email: Rep[Option[String]]     = column[Option[String]]("email")
    def passwordHash: Rep[String]      = column[String]("password_hash")
    def firstName: Rep[String]         = column[String]("first_name")
    def lastName: Rep[String]          = column[String]("last_name")
    def avatarUrl: Rep[Option[String]] = column[Option[String]]("avatar_url")
    def createdAt: Rep[Instant]        = column[Instant]("created_at")

    def * : ProvenShape[UserRow] =
      (id, username, email, passwordHash, firstName, lastName, avatarUrl, createdAt).mapTo[UserRow]
  }

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
