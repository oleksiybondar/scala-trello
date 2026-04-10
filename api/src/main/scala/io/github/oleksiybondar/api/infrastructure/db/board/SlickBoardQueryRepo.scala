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
import io.github.oleksiybondar.api.infrastructure.db.SharedSlickTables.{
  BoardRow,
  BoardsTable,
  PermissionRow,
  PermissionsTable,
  RoleRow,
  RolesTable,
  TicketsTable,
  TimeTrackingTable,
  UserRow,
  UsersTable
}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

final class SlickBoardQueryRepo[F[_]: Async](db: Database) extends BoardQueryRepo[F] {

  private final case class BoardMemberRow(
      boardId: UUID,
      userId: UUID,
      roleId: Long,
      createdAt: Instant
  )

  private final class BoardMembersTable(tag: Tag)
      extends Table[BoardMemberRow](tag, "board_members") {
    def boardId: Rep[UUID]      = column[UUID]("board_id")
    def userId: Rep[UUID]       = column[UUID]("user_id")
    def roleId: Rep[Long]       = column[Long]("role_id")
    def createdAt: Rep[Instant] = column[Instant]("created_at")

    def * : ProvenShape[BoardMemberRow] = (boardId, userId, roleId, createdAt).mapTo[BoardMemberRow]
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
