package io.github.oleksiybondar.api.infrastructure.db.ticket

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.BoardId
import io.github.oleksiybondar.api.domain.ticket.{
  Ticket,
  TicketAcceptanceCriteria,
  TicketComponent,
  TicketDescription,
  TicketId,
  TicketName,
  TicketPriority,
  TicketScope,
  TicketSeverityId,
  TicketStateId
}
import io.github.oleksiybondar.api.domain.user.UserId
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

final class SlickTicketRepo[F[_]: Async](db: Database) extends TicketRepo[F] {

  private final case class TicketRow(
      id: Long,
      boardId: UUID,
      name: String,
      description: Option[String],
      component: Option[String],
      scope: Option[String],
      acceptanceCriteria: Option[String],
      createdByUserId: UUID,
      assignedToUserId: Option[UUID],
      lastModifiedByUserId: UUID,
      createdAt: Instant,
      modifiedAt: Instant,
      originalEstimatedMinutes: Option[Int],
      priority: Option[String],
      severityId: Option[Long],
      stateId: Long,
      commentsEnabled: Boolean
  )

  private final class TicketsTable(tag: Tag) extends Table[TicketRow](tag, "tickets") {
    def id: Rep[Long]                              = column[Long]("id", O.PrimaryKey)
    def boardId: Rep[UUID]                         = column[UUID]("dashboard_id")
    def name: Rep[String]                          = column[String]("name")
    def description: Rep[Option[String]]           = column[Option[String]]("description")
    def component: Rep[Option[String]]             = column[Option[String]]("component")
    def scope: Rep[Option[String]]                 = column[Option[String]]("scope")
    def acceptanceCriteria: Rep[Option[String]]    = column[Option[String]]("acceptance_criteria")
    def createdByUserId: Rep[UUID]                 = column[UUID]("created_by_user_id")
    def assignedToUserId: Rep[Option[UUID]]        = column[Option[UUID]]("assigned_to_user_id")
    def lastModifiedByUserId: Rep[UUID]            = column[UUID]("last_modified_by_user_id")
    def createdAt: Rep[Instant]                    = column[Instant]("created_at")
    def modifiedAt: Rep[Instant]                   = column[Instant]("modified_at")
    def originalEstimatedMinutes: Rep[Option[Int]] =
      column[Option[Int]]("original_estimated_minutes")
    def priority: Rep[Option[String]]              = column[Option[String]]("priority")
    def severityId: Rep[Option[Long]]              = column[Option[Long]]("severity_id")
    def stateId: Rep[Long]                         = column[Long]("state_id")
    def commentsEnabled: Rep[Boolean]              = column[Boolean]("comments_enabled")

    def * : ProvenShape[TicketRow] =
      (
        id,
        boardId,
        name,
        description,
        component,
        scope,
        acceptanceCriteria,
        createdByUserId,
        assignedToUserId,
        lastModifiedByUserId,
        createdAt,
        modifiedAt,
        originalEstimatedMinutes,
        priority,
        severityId,
        stateId,
        commentsEnabled
      ).mapTo[TicketRow]
  }

  private val tickets = TableQuery[TicketsTable]

  override def nextId: F[TicketId] =
    run(tickets.map(_.id).max.result).map(id => TicketId(id.getOrElse(0L) + 1L))

  override def create(ticket: Ticket): F[Unit] =
    run(tickets += toRow(ticket)).void

  override def findById(id: TicketId): F[Option[Ticket]] =
    run(
      tickets
        .filter(_.id === id.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def listByBoard(boardId: BoardId): F[List[Ticket]] =
    run(
      tickets
        .filter(_.boardId === boardId.value)
        .sortBy(_.createdAt.desc)
        .result
    ).map(_.toList.map(toDomain))

  override def update(ticket: Ticket): F[Boolean] =
    run(
      tickets
        .filter(_.id === ticket.id.value)
        .update(toRow(ticket))
    ).map(_ > 0)

  override def delete(id: TicketId): F[Boolean] =
    run(
      tickets
        .filter(_.id === id.value)
        .delete
    ).map(_ > 0)

  private def toRow(ticket: Ticket): TicketRow =
    TicketRow(
      id = ticket.id.value,
      boardId = ticket.boardId.value,
      name = ticket.name.value,
      description = ticket.description.map(_.value),
      component = ticket.component.map(_.value),
      scope = ticket.scope.map(_.value),
      acceptanceCriteria = ticket.acceptanceCriteria.map(_.value),
      createdByUserId = ticket.createdByUserId.value,
      assignedToUserId = ticket.assignedToUserId.map(_.value),
      lastModifiedByUserId = ticket.lastModifiedByUserId.value,
      createdAt = ticket.createdAt,
      modifiedAt = ticket.modifiedAt,
      originalEstimatedMinutes = ticket.originalEstimatedMinutes,
      priority = ticket.priority.map(_.value),
      severityId = ticket.severityId.map(_.value),
      stateId = ticket.stateId.value,
      commentsEnabled = ticket.commentsEnabled
    )

  private def toDomain(row: TicketRow): Ticket =
    Ticket(
      id = TicketId(row.id),
      boardId = BoardId(row.boardId),
      name = TicketName(row.name),
      description = row.description.map(TicketDescription(_)),
      component = row.component.map(TicketComponent(_)),
      scope = row.scope.map(TicketScope(_)),
      acceptanceCriteria = row.acceptanceCriteria.map(TicketAcceptanceCriteria(_)),
      createdByUserId = UserId(row.createdByUserId),
      assignedToUserId = row.assignedToUserId.map(UserId(_)),
      lastModifiedByUserId = UserId(row.lastModifiedByUserId),
      createdAt = row.createdAt,
      modifiedAt = row.modifiedAt,
      originalEstimatedMinutes = row.originalEstimatedMinutes,
      priority = row.priority.map(TicketPriority(_)),
      severityId = row.severityId.map(TicketSeverityId(_)),
      stateId = TicketStateId(row.stateId),
      commentsEnabled = row.commentsEnabled
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))
}
