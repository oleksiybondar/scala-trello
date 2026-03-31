package io.github.oleksiybondar.api.infrastructure.db.auth.password

import io.github.oleksiybondar.api.domain.auth.password.{PasswordHistoryEntry, PasswordHistoryId}
import io.github.oleksiybondar.api.domain.user.UserId

trait PasswordHistoryRepo[F[_]] {
  def create(entry: PasswordHistoryEntry): F[Unit]
  def findByUserId(userId: UserId): F[List[PasswordHistoryEntry]]
  def deleteByUserId(userId: UserId): F[Unit]
  def deleteByIds(ids: List[PasswordHistoryId]): F[Unit]
}
