package io.github.oleksiybondar.api.testkit.support

import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.{Board, BoardId, BoardMember}
import io.github.oleksiybondar.api.domain.permission.{Permission, Role}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.board.{
  BoardQueryRepo,
  BoardQueryRoleRow,
  BoardQueryRow,
  BoardQueryTicketRow,
  BoardQueryUserRow
}

final class InMemoryBoardQueryRepo[F[_]: Sync](
    boards: List[Board],
    members: List[BoardMember],
    tickets: List[io.github.oleksiybondar.api.domain.ticket.Ticket],
    timeEntries: List[io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingEntry],
    users: List[io.github.oleksiybondar.api.domain.user.User],
    permissions: List[Permission],
    roles: List[Role]
) extends BoardQueryRepo[F] {

  override def findById(boardId: BoardId, currentUserId: UserId): F[Option[BoardQueryRow]] =
    boards.find(_.id == boardId).flatMap { board =>
      for {
        owner          <- users.find(_.id == board.ownerUserId)
        createdBy      <- users.find(_.id == board.createdByUserId)
        lastModifiedBy <- users.find(_.id == board.lastModifiedByUserId)
      } yield {
        val currentUserRole = members
          .find(member => member.boardId == board.id && member.userId == currentUserId)
          .flatMap(member => roles.find(_.id == member.roleId))
          .map(role =>
            BoardQueryRoleRow(
              id = role.id.value.toString,
              name = role.name.value,
              description = role.description,
              permissions = permissions.filter(_.roleId == role.id).sortBy(_.id.value)
            )
          )

        BoardQueryRow(
          id = board.id,
          name = board.name.value,
          description = board.description.map(_.value),
          active = board.active,
          ownerUserId = board.ownerUserId.value.toString,
          createdByUserId = board.createdByUserId.value.toString,
          createdAt = board.createdAt.toString,
          modifiedAt = board.modifiedAt.toString,
          lastModifiedByUserId = board.lastModifiedByUserId.value.toString,
          owner = toUserRow(owner),
          createdBy = toUserRow(createdBy),
          lastModifiedBy = toUserRow(lastModifiedBy),
          membersCount = members.count(_.boardId == board.id),
          currentUserRole = currentUserRole,
          tickets = tickets
            .filter(_.boardId == board.id)
            .sortBy(_.createdAt)(Ordering[java.time.Instant].reverse)
            .map(ticket =>
              BoardQueryTicketRow(
                id = ticket.id.value.toString,
                boardId = ticket.boardId.value.toString,
                name = ticket.name.value,
                description = ticket.description.map(_.value),
                acceptanceCriteria = ticket.acceptanceCriteria.map(_.value),
                estimatedMinutes = ticket.originalEstimatedMinutes,
                priority =
                  ticket.priority.flatMap(value => scala.util.Try(value.value.toInt).toOption),
                severityId = ticket.severityId.map(_.value),
                trackedMinutes = timeEntries
                  .filter(_.ticketId == ticket.id)
                  .map(_.durationMinutes.value)
                  .sum,
                createdByUserId = ticket.createdByUserId.value.toString,
                assignedToUserId = ticket.assignedToUserId.map(_.value.toString),
                lastModifiedByUserId = ticket.lastModifiedByUserId.value.toString,
                createdAt = ticket.createdAt.toString,
                modifiedAt = ticket.modifiedAt.toString,
                stateId = ticket.stateId
              )
            )
        )
      }
    }.pure[F]

  private def toUserRow(user: io.github.oleksiybondar.api.domain.user.User): BoardQueryUserRow =
    BoardQueryUserRow(
      id = user.id.value.toString,
      firstName = user.firstName.value,
      lastName = user.lastName.value,
      avatarUrl = user.avatarUrl.map(_.value)
    )
}
