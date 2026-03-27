package io.github.oleksiybondar.api.infrastructure.db.user

import io.github.oleksiybondar.api.domain.user._

trait UserRepo[F[_]] {
  def create(user: User): F[Unit]

  def findById(id: UserId): F[Option[User]]

  def findByUsername(username: Username): F[Option[User]]
  def findByEmail(email: Email): F[Option[User]]

  def list: F[List[User]]

  def update(user: User): F[Boolean]
  def delete(id: UserId): F[Boolean]
}
