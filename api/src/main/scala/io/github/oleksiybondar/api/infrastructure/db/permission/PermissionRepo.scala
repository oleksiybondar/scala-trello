package io.github.oleksiybondar.api.infrastructure.db.permission

import io.github.oleksiybondar.api.domain.permission.{
  Permission,
  PermissionArea,
  PermissionId,
  RoleId
}

trait PermissionRepo[F[_]] {
  def findById(id: PermissionId): F[Option[Permission]]
  def findByRoleId(roleId: RoleId): F[List[Permission]]
  def findByRoleIdAndArea(
      roleId: RoleId,
      area: PermissionArea
  ): F[Option[Permission]]
  def list: F[List[Permission]]
}
