package io.github.oleksiybondar.api.infrastructure.db.timeTracking

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.timeTracking.{TimeTrackingActivityId, TimeTrackingEntryId}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

final class SlickTimeTrackingQueryRepo[F[_]: Async](db: Database)
    extends TimeTrackingQueryRepo[F] {

  private final case class TimeTrackingRow(
      id: Long,
      ticketId: Long,
      userId: UUID,
      activityId: Long,
      durationMinutes: Int,
      loggedAt: Instant,
      description: Option[String]
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
      name: String,
      description: Option[String]
  )

  private final case class ActivityRow(
      id: Long,
      code: String,
      name: String,
      description: Option[String]
  )

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
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[TicketRow] = (id, name, description).mapTo[TicketRow]
  }

  private final class ActivitiesTable(tag: Tag) extends Table[ActivityRow](tag, "activities") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def code: Rep[String]                = column[String]("code")
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[ActivityRow] = (id, code, name, description).mapTo[ActivityRow]
  }

  private val timeTrackingEntries = TableQuery[TimeTrackingTable]
  private val users               = TableQuery[UsersTable]
  private val tickets             = TableQuery[TicketsTable]
  private val activities          = TableQuery[ActivitiesTable]

  override def listByTicket(ticketId: TicketId): F[List[TimeTrackingQueryRow]] =
    run(
      timeTrackingEntries
        .filter(_.ticketId === ticketId.value)
        .join(users)
        .on(_.userId === _.id)
        .join(activities)
        .on(_._1.activityId === _.id)
        .join(tickets)
        .on(_._1._1.ticketId === _.id)
        .sortBy(_._1._1._1.loggedAt.desc)
        .result
    ).map(_.toList.map { case (((entry, user), activity), ticket) =>
      TimeTrackingQueryRow(
        id = TimeTrackingEntryId(entry.id),
        ticketId = TicketId(entry.ticketId),
        userId = entry.userId.toString,
        activityId = TimeTrackingActivityId(entry.activityId),
        activityCode = Some(activity.code),
        activityName = Some(activity.name),
        durationMinutes = entry.durationMinutes,
        loggedAt = entry.loggedAt.toString,
        description = entry.description,
        username = user.username,
        email = user.email,
        firstName = user.firstName,
        lastName = user.lastName,
        avatarUrl = user.avatarUrl,
        userCreatedAt = user.createdAt.toString,
        ticketTitle = ticket.name,
        ticketDescription = ticket.description
      )
    })

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))
}
