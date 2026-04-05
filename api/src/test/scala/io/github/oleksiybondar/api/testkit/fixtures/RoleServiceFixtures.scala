package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.permission.{Permission, Role, RoleServiceLive}
import io.github.oleksiybondar.api.testkit.support.{InMemoryPermissionRepo, InMemoryRoleRepo}

object RoleServiceFixtures {

  final case class RoleServiceContext(
      roleRepo: InMemoryRoleRepo[IO],
      permissionRepo: InMemoryPermissionRepo[IO],
      roleService: RoleServiceLive[IO]
  )

  def withRoleService[A](
      roles: List[Role] = Nil,
      permissions: List[Permission] = Nil
  )(run: RoleServiceContext => IO[A]): A =
    (
      for {
        roleRepo       <- InMemoryRoleRepo.create[IO](roles)
        permissionRepo <- InMemoryPermissionRepo.create[IO](permissions)
        roleService     = new RoleServiceLive[IO](roleRepo, permissionRepo)
        result         <- run(RoleServiceContext(roleRepo, permissionRepo, roleService))
      } yield result
    ).unsafeRunSync()
}
