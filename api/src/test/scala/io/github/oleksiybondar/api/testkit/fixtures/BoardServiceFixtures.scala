package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.board.{
  Board,
  BoardAccessServiceLive,
  BoardMember,
  BoardMembershipServiceLive,
  BoardServiceLive
}
import io.github.oleksiybondar.api.domain.permission.{Permission, Role, RoleServiceLive}
import io.github.oleksiybondar.api.testkit.support.{
  InMemoryBoardMemberRepo,
  InMemoryBoardRepo,
  InMemoryPermissionRepo,
  InMemoryRoleRepo
}

object BoardServiceFixtures {

  final case class BoardServiceContext(
      dashboardRepo: InMemoryBoardRepo[IO],
      dashboardMemberRepo: InMemoryBoardMemberRepo[IO],
      roleService: RoleServiceLive[IO],
      dashboardMembershipService: BoardMembershipServiceLive[IO],
      dashboardAccessService: BoardAccessServiceLive[IO],
      dashboardService: BoardServiceLive[IO]
  )

  def withBoardService[A](
      dashboards: List[Board] = Nil,
      members: List[BoardMember] = Nil,
      roles: List[Role] = Nil,
      permissions: List[Permission] = Nil
  )(run: BoardServiceContext => IO[A]): A =
    (
      for {
        dashboardRepo             <- InMemoryBoardRepo.create[IO](dashboards)
        dashboardMemberRepo       <- InMemoryBoardMemberRepo.create[IO](members)
        roleRepo                  <- InMemoryRoleRepo.create[IO](roles)
        permissionRepo            <- InMemoryPermissionRepo.create[IO](permissions)
        roleService                = new RoleServiceLive[IO](roleRepo, permissionRepo)
        dashboardMembershipService = new BoardMembershipServiceLive[IO](
                                       dashboardMemberRepo,
                                       roleService
                                     )
        dashboardAccessService     = new BoardAccessServiceLive[IO](
                                       dashboardRepo,
                                       dashboardMembershipService
                                     )
        dashboardService           = new BoardServiceLive[IO](
                                       dashboardRepo,
                                       dashboardAccessService,
                                       dashboardMembershipService,
                                       roleService
                                     )
        result                    <- run(
                                       BoardServiceContext(
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
