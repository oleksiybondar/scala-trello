package io.github.oleksiybondar.api.domain.auth.password

import io.github.oleksiybondar.api.domain.user.PasswordHash

trait PasswordHasher[F[_]] {
  def hash(password: String): F[PasswordHash]
  def verify(password: String, hash: PasswordHash): F[Boolean]
}
