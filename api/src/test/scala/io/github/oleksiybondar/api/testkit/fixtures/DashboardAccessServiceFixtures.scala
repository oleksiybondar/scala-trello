package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.dashboard.{
  Dashboard,
  DashboardAccessServiceLive,
  DashboardMember
}
import io.github.oleksiybondar.api.domain.permission.{Permission, Role}
import io.github.oleksiybondar.api.testkit.support.InMemoryDashboardRepo

object DashboardAccessServiceFixtures {

  final case class DashboardAccessServiceContext(
      dashboardRepo: InMemoryDashboardRepo[IO],
      membershipFixtures: DashboardMembershipServiceFixtures.DashboardMembershipServiceContext,
      dashboardAccessService: DashboardAccessServiceLive[IO]
  )

  def withDashboardAccessService[A](
      dashboards: List[Dashboard] = Nil,
      members: List[DashboardMember] = Nil,
      roles: List[Role] = Nil,
      permissions: List[Permission] = Nil
  )(run: DashboardAccessServiceContext => IO[A]): A =
    (
      for {
        dashboardRepo <- InMemoryDashboardRepo.create[IO](dashboards)
        result        <- IO.pure(
                           DashboardMembershipServiceFixtures.withDashboardMembershipService(
                             members = members,
                             roles = roles,
                             permissions = permissions
                           ) { membershipCtx =>
                             run(
                               DashboardAccessServiceContext(
                                 dashboardRepo = dashboardRepo,
                                 membershipFixtures = membershipCtx,
                                 dashboardAccessService = new DashboardAccessServiceLive[IO](
                                   dashboardRepo,
                                   membershipCtx.dashboardMembershipService
                                 )
                               )
                             )
                           }
                         )
      } yield result
    ).unsafeRunSync()
}
