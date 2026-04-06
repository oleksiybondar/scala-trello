package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import io.github.oleksiybondar.api.domain.dashboard.{DashboardAccessServiceLive, DashboardMember}
import io.github.oleksiybondar.api.domain.permission.{Permission, Role}

object DashboardAccessServiceFixtures {

  final case class DashboardAccessServiceContext(
      membershipFixtures: DashboardMembershipServiceFixtures.DashboardMembershipServiceContext,
      dashboardAccessService: DashboardAccessServiceLive[IO]
  )

  def withDashboardAccessService[A](
      members: List[DashboardMember] = Nil,
      roles: List[Role] = Nil,
      permissions: List[Permission] = Nil
  )(run: DashboardAccessServiceContext => IO[A]): A =
    DashboardMembershipServiceFixtures.withDashboardMembershipService(
      members = members,
      roles = roles,
      permissions = permissions
    ) { membershipCtx =>
      run(
        DashboardAccessServiceContext(
          membershipFixtures = membershipCtx,
          dashboardAccessService = new DashboardAccessServiceLive[IO](
            membershipCtx.dashboardMembershipService
          )
        )
      )
    }
}
