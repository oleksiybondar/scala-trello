package io.github.oleksiybondar.api.infrastructure.db.board

import io.github.oleksiybondar.api.domain.board.{BoardId, BoardMember}
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId

trait BoardMemberRepo[F[_]] {
  def create(member: BoardMember): F[Unit]
  def findByBoardIdAndUserId(
      boardId: BoardId,
      userId: UserId
  ): F[Option[BoardMember]]
  def listByBoardId(boardId: BoardId): F[List[BoardMember]]
  def listByUserId(userId: UserId): F[List[BoardMember]]
  def updateRole(boardId: BoardId, userId: UserId, roleId: RoleId): F[Boolean]
  def delete(boardId: BoardId, userId: UserId): F[Boolean]
}
