package io.github.oleksiybondar.api.domain.permission

import cats.Monad
import cats.syntax.all._
import io.github.oleksiybondar.api.infrastructure.db.permission.{PermissionRepo, RoleRepo}

final class RoleServiceLive[F[_]: Monad](
    roleRepo: RoleRepo[F],
    permissionRepo: PermissionRepo[F]
) extends RoleService[F] {

  override def getRole(id: RoleId): F[Option[Role]] =
    roleRepo.findById(id)

  override def getByName(name: RoleName): F[Option[Role]] =
    roleRepo.findByName(name)

  override def listRoles: F[List[Role]] =
    roleRepo.list

  override def getRoleWithPermissions(id: RoleId): F[Option[RoleWithPermissions]] =
    roleRepo.findById(id).flatMap(_.traverse(loadRoleWithPermissions))

  override def getRoleWithPermissionsByName(name: RoleName): F[Option[RoleWithPermissions]] =
    roleRepo.findByName(name).flatMap(_.traverse(loadRoleWithPermissions))

  override def listRolesWithPermissions: F[List[RoleWithPermissions]] =
    roleRepo.list.flatMap(_.traverse(loadRoleWithPermissions))

  private def loadRoleWithPermissions(role: Role): F[RoleWithPermissions] =
    permissionRepo
      .findByRoleId(role.id)
      .map(permissions => RoleWithPermissions(role, permissions))
}
