package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.dashboard.{
  Dashboard,
  DashboardAccessServiceLive,
  DashboardMember,
  DashboardMembershipServiceLive,
  DashboardServiceLive
}
import io.github.oleksiybondar.api.domain.permission.{Permission, Role, RoleServiceLive}
import io.github.oleksiybondar.api.testkit.support.{
  InMemoryDashboardMemberRepo,
  InMemoryDashboardRepo,
  InMemoryPermissionRepo,
  InMemoryRoleRepo
}

object DashboardServiceFixtures {

  final case class DashboardServiceContext(
      dashboardRepo: InMemoryDashboardRepo[IO],
      dashboardMemberRepo: InMemoryDashboardMemberRepo[IO],
      roleService: RoleServiceLive[IO],
      dashboardMembershipService: DashboardMembershipServiceLive[IO],
      dashboardAccessService: DashboardAccessServiceLive[IO],
      dashboardService: DashboardServiceLive[IO]
  )

  def withDashboardService[A](
      dashboards: List[Dashboard] = Nil,
      members: List[DashboardMember] = Nil,
      roles: List[Role] = Nil,
      permissions: List[Permission] = Nil
  )(run: DashboardServiceContext => IO[A]): A =
    (
      for {
        dashboardRepo             <- InMemoryDashboardRepo.create[IO](dashboards)
        dashboardMemberRepo       <- InMemoryDashboardMemberRepo.create[IO](members)
        roleRepo                  <- InMemoryRoleRepo.create[IO](roles)
        permissionRepo            <- InMemoryPermissionRepo.create[IO](permissions)
        roleService                = new RoleServiceLive[IO](roleRepo, permissionRepo)
        dashboardMembershipService = new DashboardMembershipServiceLive[IO](
                                       dashboardMemberRepo,
                                       roleService
                                     )
        dashboardAccessService     = new DashboardAccessServiceLive[IO](
                                       dashboardRepo,
                                       dashboardMembershipService
                                     )
        dashboardService           = new DashboardServiceLive[IO](
                                       dashboardRepo,
                                       dashboardAccessService,
                                       dashboardMembershipService,
                                       roleService
                                     )
        result                    <- run(
                                       DashboardServiceContext(
                                         dashboardRepo = dashboardRepo,
                                         dashboardMemberRepo = dashboardMemberRepo,
                                         roleService = roleService,
                                         dashboardMembershipService = dashboardMembershipService,
                                         dashboardAccessService = dashboardAccessService,
                                         dashboardService = dashboardService
                                       )
                                     )
      } yield result
    ).unsafeRunSync()
}
