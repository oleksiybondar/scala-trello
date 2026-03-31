package io.github.oleksiybondar.api.domain.auth.password

import io.github.oleksiybondar.api.domain.user.{PasswordHash, UserId}

import java.time.Instant
import java.util.UUID

/** Stable identifier of a single stored password-history record. */
final case class PasswordHistoryId(value: UUID) extends AnyVal

/** Stored historical password hash associated with a user. */
final case class PasswordHistoryEntry(
    /** Unique identifier of the history record. */
    id: PasswordHistoryId,
    /** User whose previous password is being tracked. */
    userId: UserId,
    /** Historical password hash retained for reuse-prevention checks. */
    passwordHash: PasswordHash,
    /** Timestamp when this password hash was recorded into history. */
    createdAt: Instant
)

/** Business service responsible for password-reuse prevention. */
trait PasswordHistory[F[_]] {

  /** Adds a password hash to the user's history and applies retention rules.
    *
    * @param userId
    *   User whose history should be extended.
    * @param hash
    *   Password hash that should become part of the historical record.
    */
  def record(userId: UserId, hash: PasswordHash): F[Unit]

  /** Checks whether the provided raw password matches any retained historical password.
    *
    * @param userId
    *   User whose password history should be inspected.
    * @param password
    *   Raw password candidate that should be rejected if it was used before.
    * @return
    *   `true` when the password matches a historical hash, `false` otherwise.
    */
  def wasUsedBefore(userId: UserId, password: String): F[Boolean]

  /** Removes all stored password-history records for a user.
    *
    * @param userId
    *   User whose password history should be deleted.
    */
  def clear(userId: UserId): F[Unit]
}
