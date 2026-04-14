package io.github.oleksiybondar.api.infrastructure.db.board

import io.github.oleksiybondar.api.domain.board.BoardId
import io.github.oleksiybondar.api.domain.permission.Permission
import io.github.oleksiybondar.api.domain.ticket.TicketStateId
import io.github.oleksiybondar.api.domain.user.UserId

final case class BoardQueryUserRow(
    id: String,
    firstName: String,
    lastName: String,
    avatarUrl: Option[String]
)

final case class BoardQueryRoleRow(
    id: String,
    name: String,
    description: Option[String],
    permissions: List[Permission]
)

final case class BoardQueryTicketRow(
    id: String,
    boardId: String,
    name: String,
    description: Option[String],
    acceptanceCriteria: Option[String],
    estimatedMinutes: Option[Int],
    priority: Option[Int],
    severityId: Option[Long],
    trackedMinutes: Int,
    createdByUserId: String,
    assignedToUserId: Option[String],
    lastModifiedByUserId: String,
    createdAt: String,
    modifiedAt: String,
    stateId: TicketStateId
)

final case class BoardQueryRow(
    id: BoardId,
    name: String,
    description: Option[String],
    active: Boolean,
    ownerUserId: String,
    createdByUserId: String,
    createdAt: String,
    modifiedAt: String,
    lastModifiedByUserId: String,
    owner: BoardQueryUserRow,
    createdBy: BoardQueryUserRow,
    lastModifiedBy: BoardQueryUserRow,
    membersCount: Int,
    currentUserRole: Option[BoardQueryRoleRow],
    tickets: List[BoardQueryTicketRow]
)

trait BoardQueryRepo[F[_]] {
  def findById(boardId: BoardId, currentUserId: UserId): F[Option[BoardQueryRow]]
}
