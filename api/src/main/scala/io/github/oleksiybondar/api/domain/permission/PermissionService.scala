package io.github.oleksiybondar.api.domain.permission

/** Read-oriented service over permission reference data.
  *
  * At the current stage this service is a thin wrapper over `PermissionRepo` and does not add
  * business logic of its own.
  *
  * The abstraction is kept intentionally for two reasons:
  *   - consistency with the surrounding role/permission service layer
  *   - future scalability, where permissions may evolve beyond seeded dictionary values and gain
  *     dedicated administration flows
  *
  * Keeping this boundary now avoids later refactoring when permission management becomes more than
  * direct repository access.
  */
trait PermissionService[F[_]] {

  /** Loads a permission by id. */
  def getPermission(id: PermissionId): F[Option[Permission]]

  /** Lists all permissions. */
  def listPermissions: F[List[Permission]]

  /** Lists permissions assigned to a specific role. */
  def listPermissionsByRoleId(roleId: RoleId): F[List[Permission]]
}
