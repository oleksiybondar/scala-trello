package io.github.oleksiybondar.api.infrastructure.db.ticket

import cats.effect.Async
import io.github.oleksiybondar.api.domain.ticket.{TicketId, TicketStateId}
import io.github.oleksiybondar.api.domain.timeTracking.{TimeTrackingActivityId, TimeTrackingEntryId}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Rep}

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

final class SlickTicketQueryRepo[F[_]: Async](db: Database) extends TicketQueryRepo[F] {

  private final case class TicketRow(
      id: Long,
      boardId: UUID,
      name: String,
      description: Option[String],
      acceptanceCriteria: Option[String],
      createdByUserId: UUID,
      assignedToUserId: Option[UUID],
      lastModifiedByUserId: UUID,
      createdAt: Instant,
      modifiedAt: Instant,
      originalEstimatedMinutes: Option[Int],
      stateId: Long
  )

  private final case class BoardRow(
      id: UUID,
      name: String,
      active: Boolean
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

  private final case class CommentRow(
      id: Long,
      ticketId: Long
  )

  private final case class TimeTrackingRow(
      id: Long,
      ticketId: Long,
      userId: UUID,
      activityId: Long,
      durationMinutes: Int,
      loggedAt: Instant,
      description: Option[String]
  )

  private final case class ActivityRow(
      id: Long,
      code: String,
      name: String,
      description: Option[String]
  )

  private final class TicketsTable(tag: Tag) extends Table[TicketRow](tag, "tickets") {
    def id: Rep[Long]                              = column[Long]("id", O.PrimaryKey)
    def boardId: Rep[UUID]                         = column[UUID]("dashboard_id")
    def name: Rep[String]                          = column[String]("name")
    def description: Rep[Option[String]]           = column[Option[String]]("description")
    def acceptanceCriteria: Rep[Option[String]]    = column[Option[String]]("acceptance_criteria")
    def createdByUserId: Rep[UUID]                 = column[UUID]("created_by_user_id")
    def assignedToUserId: Rep[Option[UUID]]        = column[Option[UUID]]("assigned_to_user_id")
    def lastModifiedByUserId: Rep[UUID]            = column[UUID]("last_modified_by_user_id")
    def createdAt: Rep[Instant]                    = column[Instant]("created_at")
    def modifiedAt: Rep[Instant]                   = column[Instant]("modified_at")
    def originalEstimatedMinutes: Rep[Option[Int]] =
      column[Option[Int]]("original_estimated_minutes")
    def stateId: Rep[Long]                         = column[Long]("state_id")

    def * : ProvenShape[TicketRow] =
      (
        id,
        boardId,
        name,
        description,
        acceptanceCriteria,
        createdByUserId,
        assignedToUserId,
        lastModifiedByUserId,
        createdAt,
        modifiedAt,
        originalEstimatedMinutes,
        stateId
      ).mapTo[TicketRow]
  }

  private final class BoardsTable(tag: Tag) extends Table[BoardRow](tag, "dashboards") {
    def id: Rep[UUID]        = column[UUID]("id", O.PrimaryKey)
    def name: Rep[String]    = column[String]("name")
    def active: Rep[Boolean] = column[Boolean]("active")

    def * : ProvenShape[BoardRow] = (id, name, active).mapTo[BoardRow]
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

  private final class CommentsTable(tag: Tag) extends Table[CommentRow](tag, "comments") {
    def id: Rep[Long]       = column[Long]("id", O.PrimaryKey)
    def ticketId: Rep[Long] = column[Long]("ticket_id")

    def * : ProvenShape[CommentRow] = (id, ticketId).mapTo[CommentRow]
  }

  private final class TimeTrackingTable(tag: Tag)
      extends Table[TimeTrackingRow](tag, "time_tracking") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def ticketId: Rep[Long]              = column[Long]("ticket_id")
    def userId: Rep[UUID]                = column[UUID]("user_id")
    def activityId: Rep[Long]            = column[Long]("activity_id")
    def durationMinutes: Rep[Int]        = column[Int]("duration_minutes")
    def loggedAt: Rep[Instant]           = column[Instant]("logged_at")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[TimeTrackingRow] = (
      id,
      ticketId,
      userId,
      activityId,
      durationMinutes,
      loggedAt,
      description
    ).mapTo[TimeTrackingRow]
  }

  private final class ActivitiesTable(tag: Tag) extends Table[ActivityRow](tag, "activities") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def code: Rep[String]                = column[String]("code")
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[ActivityRow] = (id, code, name, description).mapTo[ActivityRow]
  }

  private val tickets      = TableQuery[TicketsTable]
  private val boards       = TableQuery[BoardsTable]
  private val users        = TableQuery[UsersTable]
  private val comments     = TableQuery[CommentsTable]
  private val timeTracking = TableQuery[TimeTrackingTable]
  private val activities   = TableQuery[ActivitiesTable]

  override def findById(ticketId: TicketId): F[Option[TicketQueryRow]] =
    run(
      for {
        detail <- ticketDetailQuery(ticketId).result.headOption
        times  <- ticketTimeEntriesQuery(ticketId).result
      } yield detail.map { case (ticket, board, createdBy, assignedTo, modifiedBy, commentsCount) =>
        TicketQueryRow(
          id = TicketId(ticket.id),
          boardId = ticket.boardId.toString,
          name = ticket.name,
          description = ticket.description,
          acceptanceCriteria = ticket.acceptanceCriteria,
          estimatedMinutes = ticket.originalEstimatedMinutes,
          createdByUserId = ticket.createdByUserId.toString,
          assignedToUserId = ticket.assignedToUserId.map(_.toString),
          lastModifiedByUserId = ticket.lastModifiedByUserId.toString,
          createdAt = ticket.createdAt.toString,
          modifiedAt = ticket.modifiedAt.toString,
          stateId = TicketStateId(ticket.stateId),
          commentsCount = commentsCount,
          board = TicketQueryBoardRow(
            id = board.id.toString,
            name = board.name,
            active = board.active
          ),
          createdBy = toUserRow(createdBy),
          assignedTo = assignedTo.map(toUserRow),
          lastModifiedBy = toUserRow(modifiedBy),
          timeEntries = times.toList.map { case ((entry, user), activity) =>
            TicketQueryTimeEntryRow(
              id = TimeTrackingEntryId(entry.id),
              ticketId = TicketId(entry.ticketId),
              userId = entry.userId.toString,
              activityId = TimeTrackingActivityId(entry.activityId),
              activityCode = activity.code,
              activityName = activity.name,
              durationMinutes = entry.durationMinutes,
              loggedAt = entry.loggedAt.toString,
              description = entry.description,
              user = toUserRow(user)
            )
          }
        )
      }
    )

  private def ticketDetailQuery(ticketId: TicketId) =
    tickets
      .filter(_.id === ticketId.value)
      .join(boards)
      .on(_.boardId === _.id)
      .join(users)
      .on(_._1.createdByUserId === _.id)
      .joinLeft(users)
      .on(_._1._1.assignedToUserId === _.id)
      .join(users)
      .on(_._1._1._1.lastModifiedByUserId === _.id)
      .joinLeft(
        comments
          .groupBy(_.ticketId)
          .map { case (groupTicketId, groupComments) =>
            groupTicketId -> groupComments.length
          }
      )
      .on(_._1._1._1._1.id === _._1)
      .map { case (((((ticket, board), createdBy), assignedTo), modifiedBy), commentAggregate) =>
        (
          ticket,
          board,
          createdBy,
          assignedTo,
          modifiedBy,
          commentAggregate.map(_._2).getOrElse(0)
        )
      }

  private def ticketTimeEntriesQuery(ticketId: TicketId) =
    timeTracking
      .filter(_.ticketId === ticketId.value)
      .join(users)
      .on(_.userId === _.id)
      .join(activities)
      .on(_._1.activityId === _.id)
      .sortBy(_._1._1.loggedAt.desc)

  private def toUserRow(user: UserRow): TicketQueryUserRow =
    TicketQueryUserRow(
      id = user.id.toString,
      username = user.username,
      email = user.email,
      firstName = user.firstName,
      lastName = user.lastName,
      avatarUrl = user.avatarUrl,
      createdAt = user.createdAt.toString
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action.transactionally)))
}
