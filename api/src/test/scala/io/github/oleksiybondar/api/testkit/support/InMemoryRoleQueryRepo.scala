package io.github.oleksiybondar.api.testkit.support

import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.permission.{Permission, Role, RoleId, RoleWithPermissions}
import io.github.oleksiybondar.api.infrastructure.db.permission.RoleQueryRepo

final class InMemoryRoleQueryRepo[F[_]: Sync](
    roles: List[Role],
    permissions: List[Permission]
) extends RoleQueryRepo[F] {

  override def findById(id: RoleId): F[Option[RoleWithPermissions]] =
    roles.find(_.id == id).map(toRoleWithPermissions).pure[F]

  override def list: F[List[RoleWithPermissions]] =
    roles.sortBy(_.id.value).map(toRoleWithPermissions).pure[F]

  private def toRoleWithPermissions(role: Role): RoleWithPermissions =
    RoleWithPermissions(
      role = role,
      permissions = permissions.filter(_.roleId == role.id).sortBy(_.id.value)
    )
}
