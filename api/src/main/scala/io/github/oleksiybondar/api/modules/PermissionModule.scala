package io.github.oleksiybondar.api.modules

import cats.effect.Async
import io.github.oleksiybondar.api.domain.permission.{
  PermissionService,
  PermissionServiceLive,
  RoleService,
  RoleServiceLive
}
import io.github.oleksiybondar.api.infrastructure.db.permission.{
  PermissionRepo,
  RoleQueryRepo,
  RoleRepo,
  SlickPermissionRepo,
  SlickRoleQueryRepo,
  SlickRoleRepo
}
import slick.jdbc.PostgresProfile.api.Database

final case class PermissionModule[F[_]](
    roleRepo: RoleRepo[F],
    permissionRepo: PermissionRepo[F],
    roleQueryRepo: RoleQueryRepo[F],
    roleService: RoleService[F],
    permissionService: PermissionService[F]
)

object PermissionModule {

  def make[F[_]: Async](db: Database): PermissionModule[F] = {
    val roleRepo       = new SlickRoleRepo[F](db)
    val permissionRepo = new SlickPermissionRepo[F](db)
    val roleQueryRepo  = new SlickRoleQueryRepo[F](db)
    val roleService    = new RoleServiceLive[F](roleRepo, permissionRepo)

    PermissionModule(
      roleRepo = roleRepo,
      permissionRepo = permissionRepo,
      roleQueryRepo = roleQueryRepo,
      roleService = roleService,
      permissionService = new PermissionServiceLive[F](permissionRepo)
    )
  }
}
