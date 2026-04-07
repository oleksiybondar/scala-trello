package io.github.oleksiybondar.api.domain.permission

final case class RoleWithPermissions(
    role: Role,
    permissions: List[Permission]
) {

  def canRead(area: PermissionArea): Boolean =
    permissionFor(area).exists(_.canRead)

  def canCreate(area: PermissionArea): Boolean =
    permissionFor(area).exists(_.canCreate)

  def canModify(area: PermissionArea): Boolean =
    permissionFor(area).exists(_.canModify)

  def canDelete(area: PermissionArea): Boolean =
    permissionFor(area).exists(_.canDelete)

  def canReassign(area: PermissionArea): Boolean =
    permissionFor(area).exists(_.canReassign)

  private def permissionFor(area: PermissionArea): Option[Permission] =
    permissions.find(_.area == area)
}
