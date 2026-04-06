package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.dashboard.{
  DashboardMember,
  DashboardMembershipServiceLive
}
import io.github.oleksiybondar.api.domain.permission.{Permission, Role}
import io.github.oleksiybondar.api.testkit.support.{
  InMemoryDashboardMemberRepo,
  InMemoryPermissionRepo,
  InMemoryRoleRepo
}

object DashboardMembershipServiceFixtures {

  final case class DashboardMembershipServiceContext(
      dashboardMemberRepo: InMemoryDashboardMemberRepo[IO],
      roleServiceFixtures: RoleServiceFixtures.RoleServiceContext,
      dashboardMembershipService: DashboardMembershipServiceLive[IO]
  )

  def withDashboardMembershipService[A](
      members: List[DashboardMember] = Nil,
      roles: List[Role] = Nil,
      permissions: List[Permission] = Nil
  )(run: DashboardMembershipServiceContext => IO[A]): A =
    (
      for {
        dashboardMemberRepo <- InMemoryDashboardMemberRepo.create[IO](members)
        roleRepo            <- InMemoryRoleRepo.create[IO](roles)
        permissionRepo      <- InMemoryPermissionRepo.create[IO](permissions)
        roleService          = new io.github.oleksiybondar.api.domain.permission.RoleServiceLive[IO](
                                 roleRepo,
                                 permissionRepo
                               )
        membershipService    = new DashboardMembershipServiceLive[IO](
                                 dashboardMemberRepo,
                                 roleService
                               )
        result              <- run(
                                 DashboardMembershipServiceContext(
                                   dashboardMemberRepo = dashboardMemberRepo,
                                   roleServiceFixtures =
                                     RoleServiceFixtures.RoleServiceContext(
                                       roleRepo,
                                       permissionRepo,
                                       roleService
                                     ),
                                   dashboardMembershipService = membershipService
                                 )
                               )
      } yield result
    ).unsafeRunSync()
}
