package io.github.oleksiybondar.api.infrastructure.db.timeTracking

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.timeTracking.{TimeTrackingActivityId, TimeTrackingEntryId}
import io.github.oleksiybondar.api.infrastructure.db.SharedSlickTables.{
  ActivitiesTable,
  TimeTrackingTable,
  UsersTable
}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

final class SlickTimeTrackingQueryRepo[F[_]: Async](db: Database)
    extends TimeTrackingQueryRepo[F] {

  private final case class TicketRow(
      id: Long,
      name: String,
      description: Option[String]
  )

  private final class TicketsTable(tag: Tag) extends Table[TicketRow](tag, "tickets") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[TicketRow] = (id, name, description).mapTo[TicketRow]
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
