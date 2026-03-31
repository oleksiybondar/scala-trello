package io.github.oleksiybondar.api.domain.auth.password

import io.github.oleksiybondar.api.domain.user.{PasswordHash, UserId}

import java.time.Instant
import java.util.UUID

final case class PasswordHistoryId(value: UUID) extends AnyVal

final case class PasswordHistoryEntry(
    id: PasswordHistoryId,
    userId: UserId,
    passwordHash: PasswordHash,
    createdAt: Instant
)

trait PasswordHistory[F[_]] {
  def record(userId: UserId, hash: PasswordHash): F[Unit]
  def wasUsedBefore(userId: UserId, password: String): F[Boolean]
  def clear(userId: UserId): F[Unit]
}
