package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.permission.{Role, RoleId, RoleName}
import io.github.oleksiybondar.api.infrastructure.db.permission.RoleRepo

final class InMemoryRoleRepo[F[_]: Sync] private (
    state: Ref[F, Map[RoleId, Role]]
) extends RoleRepo[F] {

  override def findById(id: RoleId): F[Option[Role]] =
    state.get.map(_.get(id))

  override def findByName(name: RoleName): F[Option[Role]] =
    state.get.map(_.values.find(_.name == name))

  override def list: F[List[Role]] =
    state.get.map(_.values.toList.sortBy(_.id.value))
}

object InMemoryRoleRepo {

  def create[F[_]: Sync](roles: List[Role] = Nil): F[InMemoryRoleRepo[F]] =
    Ref
      .of[F, Map[RoleId, Role]](roles.map(role => role.id -> role).toMap)
      .map(new InMemoryRoleRepo[F](_))
}
