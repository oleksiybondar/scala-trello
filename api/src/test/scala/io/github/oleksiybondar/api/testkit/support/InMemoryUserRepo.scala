package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.user._
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo

final class InMemoryUserRepo[F[_]: Sync] private (
    state: Ref[F, Map[UserId, User]]
) extends UserRepo[F] {

  override def create(user: User): F[Unit] =
    state.update(_.updated(user.id, user))

  override def findById(id: UserId): F[Option[User]] =
    state.get.map(_.get(id))

  override def findByUsername(username: Username): F[Option[User]] =
    state.get.map(_.values.find(_.username.contains(username)))

  override def findByEmail(email: Email): F[Option[User]] =
    state.get.map(_.values.find(_.email.contains(email)))

  override def list: F[List[User]] =
    state.get.map(_.values.toList)

  override def update(user: User): F[Boolean] =
    state.modify { current =>
      if (current.contains(user.id))
        (current.updated(user.id, user), true)
      else
        (current, false)
    }

  override def delete(id: UserId): F[Boolean] =
    state.modify { current =>
      if (current.contains(id))
        (current - id, true)
      else
        (current, false)
    }
}

object InMemoryUserRepo {

  def create[F[_]: Sync](users: List[User] = Nil): F[InMemoryUserRepo[F]] =
    Ref
      .of[F, Map[UserId, User]](users.map(user => user.id -> user).toMap)
      .map(new InMemoryUserRepo[F](_))
}
