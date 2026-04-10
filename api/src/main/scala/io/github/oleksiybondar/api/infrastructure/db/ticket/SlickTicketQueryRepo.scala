package io.github.oleksiybondar.api.infrastructure.db.ticket

import cats.effect.Async
import io.github.oleksiybondar.api.domain.ticket.{TicketId, TicketStateId}
import io.github.oleksiybondar.api.domain.timeTracking.{TimeTrackingActivityId, TimeTrackingEntryId}
import io.github.oleksiybondar.api.infrastructure.db.SharedSlickTables.{
  ActivitiesTable,
  TicketsTable,
  TimeTrackingTable,
  UserRow,
  UsersTable
}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Rep}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

final class SlickTicketQueryRepo[F[_]: Async](db: Database) extends TicketQueryRepo[F] {

  private final case class BoardRow(
      id: UUID,
      name: String,
      active: Boolean
  )

  private final case class CommentRow(
      id: Long,
      ticketId: Long
  )

  private final class BoardsTable(tag: Tag) extends Table[BoardRow](tag, "boards") {
    def id: Rep[UUID]        = column[UUID]("id", O.PrimaryKey)
    def name: Rep[String]    = column[String]("name")
    def active: Rep[Boolean] = column[Boolean]("active")

    def * : ProvenShape[BoardRow] = (id, name, active).mapTo[BoardRow]
  }

  private final class CommentsTable(tag: Tag) extends Table[CommentRow](tag, "comments") {
    def id: Rep[Long]       = column[Long]("id", O.PrimaryKey)
    def ticketId: Rep[Long] = column[Long]("ticket_id")

    def * : ProvenShape[CommentRow] = (id, ticketId).mapTo[CommentRow]
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
