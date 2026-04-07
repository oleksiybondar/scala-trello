package io.github.oleksiybondar.api.domain.permission

import io.github.oleksiybondar.api.infrastructure.db.permission.PermissionRepo

/** Default `PermissionService` implementation delegating to `PermissionRepo`.
  *
  * This implementation is intentionally thin for now and exists to preserve a stable service
  * boundary around permissions before richer permission-management logic is introduced.
  */
final class PermissionServiceLive[F[_]](
    permissionRepo: PermissionRepo[F]
) extends PermissionService[F] {

  override def getPermission(id: PermissionId): F[Option[Permission]] =
    permissionRepo.findById(id)

  override def listPermissions: F[List[Permission]] =
    permissionRepo.list

  override def listPermissionsByRoleId(roleId: RoleId): F[List[Permission]] =
    permissionRepo.findByRoleId(roleId)
}
