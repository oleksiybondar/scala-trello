package io.github.oleksiybondar.api.infrastructure.db.board

import cats.effect.Async
import io.github.oleksiybondar.api.domain.board.BoardId
import io.github.oleksiybondar.api.domain.permission.{
  Permission,
  PermissionArea,
  PermissionId,
  RoleId
}
import io.github.oleksiybondar.api.domain.ticket.TicketStateId
import io.github.oleksiybondar.api.domain.user.UserId
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

final class SlickBoardQueryRepo[F[_]: Async](db: Database) extends BoardQueryRepo[F] {

  private final case class BoardRow(
      id: UUID,
      name: String,
      description: Option[String],
      active: Boolean,
      ownerUserId: UUID,
      createdByUserId: UUID,
      createdAt: Instant,
      modifiedAt: Instant,
      lastModifiedByUserId: UUID
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

  private final case class BoardMemberRow(
      boardId: UUID,
      userId: UUID,
      roleId: Long,
      createdAt: Instant
  )

  private final case class RoleRow(
      id: Long,
      name: String,
      description: Option[String]
  )

  private final case class PermissionRow(
      id: Long,
      roleId: Long,
      area: String,
      canRead: Boolean,
      canCreate: Boolean,
      canModify: Boolean,
      canDelete: Boolean,
      canReassign: Boolean
  )

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

  private final case class TimeTrackingRow(
      id: Long,
      ticketId: Long,
      userId: UUID,
      activityId: Long,
      durationMinutes: Int,
      loggedAt: Instant,
      description: Option[String]
  )

  private final class BoardsTable(tag: Tag) extends Table[BoardRow](tag, "boards") {
    def id: Rep[UUID]                    = column[UUID]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")
    def active: Rep[Boolean]             = column[Boolean]("active")
    def ownerUserId: Rep[UUID]           = column[UUID]("owner_user_id")
    def createdByUserId: Rep[UUID]       = column[UUID]("created_by_user_id")
    def createdAt: Rep[Instant]          = column[Instant]("created_at")
    def modifiedAt: Rep[Instant]         = column[Instant]("modified_at")
    def lastModifiedByUserId: Rep[UUID]  = column[UUID]("last_modified_by_user_id")

    def * : ProvenShape[BoardRow] =
      (
        id,
        name,
        description,
        active,
        ownerUserId,
        createdByUserId,
        createdAt,
        modifiedAt,
        lastModifiedByUserId
      ).mapTo[BoardRow]
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

  private final class BoardMembersTable(tag: Tag)
      extends Table[BoardMemberRow](tag, "board_members") {
    def boardId: Rep[UUID]      = column[UUID]("board_id")
    def userId: Rep[UUID]       = column[UUID]("user_id")
    def roleId: Rep[Long]       = column[Long]("role_id")
    def createdAt: Rep[Instant] = column[Instant]("created_at")

    def * : ProvenShape[BoardMemberRow] = (boardId, userId, roleId, createdAt).mapTo[BoardMemberRow]
  }

  private final class RolesTable(tag: Tag) extends Table[RoleRow](tag, "roles") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[RoleRow] = (id, name, description).mapTo[RoleRow]
  }

  private final class PermissionsTable(tag: Tag) extends Table[PermissionRow](tag, "permissions") {
    def id: Rep[Long]             = column[Long]("id", O.PrimaryKey)
    def roleId: Rep[Long]         = column[Long]("role_id")
    def area: Rep[String]         = column[String]("area")
    def canRead: Rep[Boolean]     = column[Boolean]("can_read")
    def canCreate: Rep[Boolean]   = column[Boolean]("can_create")
    def canModify: Rep[Boolean]   = column[Boolean]("can_modify")
    def canDelete: Rep[Boolean]   = column[Boolean]("can_delete")
    def canReassign: Rep[Boolean] = column[Boolean]("can_reassign")

    def * : ProvenShape[PermissionRow] =
      (id, roleId, area, canRead, canCreate, canModify, canDelete, canReassign).mapTo[PermissionRow]
  }

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

  private final class TimeTrackingTable(tag: Tag)
      extends Table[TimeTrackingRow](tag, "time_tracking") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def ticketId: Rep[Long]              = column[Long]("ticket_id")
    def userId: Rep[UUID]                = column[UUID]("user_id")
    def activityId: Rep[Long]            = column[Long]("activity_id")
    def durationMinutes: Rep[Int]        = column[Int]("duration_minutes")
    def loggedAt: Rep[Instant]           = column[Instant]("logged_at")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[TimeTrackingRow] =
      (
        id,
        ticketId,
        userId,
        activityId,
        durationMinutes,
        loggedAt,
        description
      ).mapTo[TimeTrackingRow]
  }

  private val boards       = TableQuery[BoardsTable]
  private val users        = TableQuery[UsersTable]
  private val boardMembers = TableQuery[BoardMembersTable]
  private val roles        = TableQuery[RolesTable]
  private val permissions  = TableQuery[PermissionsTable]
  private val tickets      = TableQuery[TicketsTable]
  private val timeTracking = TableQuery[TimeTrackingTable]

  override def findById(boardId: BoardId, currentUserId: UserId): F[Option[BoardQueryRow]] =
    run(
      for {
        boardDetailRows <- boardDetailQuery(boardId, currentUserId).result
        ticketRows      <- boardTicketsQuery(boardId).result
      } yield boardDetailRows.headOption.map { headRow =>
        val (board, owner, createdBy, lastModifiedBy, membersCount, role, _) = headRow
        val rolePermissions                                                  = boardDetailRows.flatMap(_._7)
        BoardQueryRow(
          id = BoardId(board.id),
          name = board.name,
          description = board.description,
          active = board.active,
          ownerUserId = board.ownerUserId.toString,
          createdByUserId = board.createdByUserId.toString,
          createdAt = board.createdAt.toString,
          modifiedAt = board.modifiedAt.toString,
          lastModifiedByUserId = board.lastModifiedByUserId.toString,
          owner = toUserRow(owner),
          createdBy = toUserRow(createdBy),
          lastModifiedBy = toUserRow(lastModifiedBy),
          membersCount = membersCount,
          currentUserRole = role.map(roleRow =>
            BoardQueryRoleRow(
              id = roleRow.id.toString,
              name = roleRow.name,
              description = roleRow.description,
              permissions =
                rolePermissions
                  .flatMap(toPermission)
                  .distinctBy(_.id)
                  .sortBy(_.id.value)
                  .toList
            )
          ),
          tickets = ticketRows.toList.map { case (ticket, trackedMinutes) =>
            BoardQueryTicketRow(
              id = ticket.id.toString,
              boardId = ticket.boardId.toString,
              name = ticket.name,
              description = ticket.description,
              acceptanceCriteria = ticket.acceptanceCriteria,
              estimatedMinutes = ticket.originalEstimatedMinutes,
              trackedMinutes = trackedMinutes,
              createdByUserId = ticket.createdByUserId.toString,
              assignedToUserId = ticket.assignedToUserId.map(_.toString),
              lastModifiedByUserId = ticket.lastModifiedByUserId.toString,
              createdAt = ticket.createdAt.toString,
              modifiedAt = ticket.modifiedAt.toString,
              stateId = TicketStateId(ticket.stateId)
            )
          }
        )
      }
    )

  private def boardDetailQuery(boardId: BoardId, currentUserId: UserId) =
    boards
      .filter(_.id === boardId.value)
      .join(users).on(_.ownerUserId === _.id)
      .join(users).on(_._1.createdByUserId === _.id)
      .join(users).on(_._1._1.lastModifiedByUserId === _.id)
      .joinLeft(
        boardMembers.groupBy(_.boardId).map { case (groupBoardId, members) =>
          groupBoardId -> members.length
        }
      ).on(_._1._1._1.id === _._1)
      .joinLeft(boardMembers.filter(_.userId === currentUserId.value))
      .on(_._1._1._1._1.id === _.boardId)
      .joinLeft(roles)
      .on(_._2.map(_.roleId) === _.id)
      .joinLeft(permissions)
      .on(_._2.map(_.id) === _.roleId)
      .map {
        case (
              ((((((board, owner), createdBy), lastModifiedBy), membersAggregate), member), role),
              permission
            ) =>
          (
            board,
            owner,
            createdBy,
            lastModifiedBy,
            membersAggregate.map(_._2).getOrElse(0),
            role,
            permission
          )
      }

  private def boardTicketsQuery(boardId: BoardId) =
    tickets
      .filter(_.boardId === boardId.value)
      .joinLeft(
        timeTracking
          .groupBy(_.ticketId)
          .map { case (ticketId, entries) =>
            ticketId -> entries.map(_.durationMinutes).sum.getOrElse(0)
          }
      )
      .on(_.id === _._1)
      .sortBy(_._1.createdAt.desc)
      .map { case (ticket, tracked) => (ticket, tracked.map(_._2).getOrElse(0)) }

  private def toUserRow(user: UserRow): BoardQueryUserRow =
    BoardQueryUserRow(
      id = user.id.toString,
      firstName = user.firstName,
      lastName = user.lastName,
      avatarUrl = user.avatarUrl
    )

  private def toPermission(row: PermissionRow): Option[Permission] =
    PermissionArea.fromString(row.area).map { area =>
      Permission(
        id = PermissionId(row.id),
        roleId = RoleId(row.roleId),
        area = area,
        canRead = row.canRead,
        canCreate = row.canCreate,
        canModify = row.canModify,
        canDelete = row.canDelete,
        canReassign = row.canReassign
      )
    }

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action.transactionally)))
}
