package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.permission.{
  Permission,
  PermissionArea,
  PermissionId,
  RoleId
}
import io.github.oleksiybondar.api.infrastructure.db.permission.PermissionRepo

final class InMemoryPermissionRepo[F[_]: Sync] private (
    state: Ref[F, Map[PermissionId, Permission]]
) extends PermissionRepo[F] {

  override def findById(id: PermissionId): F[Option[Permission]] =
    state.get.map(_.get(id))

  override def findByRoleId(roleId: RoleId): F[List[Permission]] =
    state.get.map(
      _.values.toList.filter(_.roleId == roleId).sortBy(_.id.value)
    )

  override def findByRoleIdAndArea(
      roleId: RoleId,
      area: PermissionArea
  ): F[Option[Permission]] =
    state.get.map(_.values.find(permission =>
      permission.roleId == roleId && permission.area == area
    ))

  override def list: F[List[Permission]] =
    state.get.map(_.values.toList.sortBy(_.id.value))
}

object InMemoryPermissionRepo {

  def create[F[_]: Sync](permissions: List[Permission] = Nil): F[InMemoryPermissionRepo[F]] =
    Ref
      .of[F, Map[PermissionId, Permission]](permissions.map(permission =>
        permission.id -> permission
      ).toMap)
      .map(new InMemoryPermissionRepo[F](_))
}
