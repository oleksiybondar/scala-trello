package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.board.{BoardMember, BoardMembershipServiceLive}
import io.github.oleksiybondar.api.domain.permission.{Permission, Role}
import io.github.oleksiybondar.api.testkit.support.{
  InMemoryBoardMemberRepo,
  InMemoryPermissionRepo,
  InMemoryRoleRepo
}

object BoardMembershipServiceFixtures {

  final case class BoardMembershipServiceContext(
      dashboardMemberRepo: InMemoryBoardMemberRepo[IO],
      roleServiceFixtures: RoleServiceFixtures.RoleServiceContext,
      dashboardMembershipService: BoardMembershipServiceLive[IO]
  )

  def withBoardMembershipService[A](
      members: List[BoardMember] = Nil,
      roles: List[Role] = Nil,
      permissions: List[Permission] = Nil
  )(run: BoardMembershipServiceContext => IO[A]): A =
    (
      for {
        dashboardMemberRepo <- InMemoryBoardMemberRepo.create[IO](members)
        roleRepo            <- InMemoryRoleRepo.create[IO](roles)
        permissionRepo      <- InMemoryPermissionRepo.create[IO](permissions)
        roleService          = new io.github.oleksiybondar.api.domain.permission.RoleServiceLive[IO](
                                 roleRepo,
                                 permissionRepo
                               )
        membershipService    = new BoardMembershipServiceLive[IO](
                                 dashboardMemberRepo,
                                 roleService
                               )
        result              <- run(
                                 BoardMembershipServiceContext(
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
