package io.github.oleksiybondar.api.domain.board

import io.github.oleksiybondar.api.domain.permission.{PermissionArea, RoleWithPermissions}

final case class BoardMemberWithRole(
    member: BoardMember,
    role: RoleWithPermissions
) {

  def canRead(area: PermissionArea): Boolean =
    role.canRead(area)

  def canCreate(area: PermissionArea): Boolean =
    role.canCreate(area)

  def canModify(area: PermissionArea): Boolean =
    role.canModify(area)

  def canDelete(area: PermissionArea): Boolean =
    role.canDelete(area)

  def canReassign(area: PermissionArea): Boolean =
    role.canReassign(area)
}
