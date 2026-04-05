package io.github.oleksiybondar.api.domain.permission

trait RoleService[F[_]] {
  def getRole(id: RoleId): F[Option[Role]]
  def getByName(name: RoleName): F[Option[Role]]
  def listRoles: F[List[Role]]
  def getRoleWithPermissions(id: RoleId): F[Option[RoleWithPermissions]]
  def getRoleWithPermissionsByName(name: RoleName): F[Option[RoleWithPermissions]]
  def listRolesWithPermissions: F[List[RoleWithPermissions]]
}
