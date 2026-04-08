package io.github.oleksiybondar.api.infrastructure.db.board

import io.github.oleksiybondar.api.domain.board.{Board, BoardId}
import io.github.oleksiybondar.api.domain.user.UserId

trait BoardRepo[F[_]] {
  def create(dashboard: Board): F[Unit]
  def findById(id: BoardId): F[Option[Board]]
  def list: F[List[Board]]
  def listByOwner(ownerUserId: UserId): F[List[Board]]
  def listByMember(userId: UserId): F[List[Board]]
  def update(dashboard: Board): F[Boolean]
  def delete(id: BoardId): F[Boolean]
}
