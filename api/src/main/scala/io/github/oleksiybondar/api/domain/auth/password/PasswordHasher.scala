package io.github.oleksiybondar.api.domain.auth.password

import io.github.oleksiybondar.api.domain.user.PasswordHash

/** Hashes and verifies passwords without exposing the underlying algorithm to callers. */
trait PasswordHasher[F[_]] {

  /** Produces a stored password hash from a raw password value.
    *
    * @param password
    *   Raw password submitted by the user.
    * @return
    *   Encoded password hash ready to persist.
    */
  def hash(password: String): F[PasswordHash]

  /** Checks whether a raw password matches a previously stored hash.
    *
    * @param password
    *   Raw password submitted for verification.
    * @param hash
    *   Persisted hash previously produced by this service.
    * @return
    *   `true` when the password matches the stored hash, `false` otherwise.
    */
  def verify(password: String, hash: PasswordHash): F[Boolean]
}
