package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.board.{Board, BoardAccessServiceLive, BoardMember}
import io.github.oleksiybondar.api.domain.permission.{Permission, Role}
import io.github.oleksiybondar.api.testkit.support.InMemoryBoardRepo

object BoardAccessServiceFixtures {

  final case class BoardAccessServiceContext(
      dashboardRepo: InMemoryBoardRepo[IO],
      membershipFixtures: BoardMembershipServiceFixtures.BoardMembershipServiceContext,
      dashboardAccessService: BoardAccessServiceLive[IO]
  )

  def withBoardAccessService[A](
      dashboards: List[Board] = Nil,
      members: List[BoardMember] = Nil,
      roles: List[Role] = Nil,
      permissions: List[Permission] = Nil
  )(run: BoardAccessServiceContext => IO[A]): A =
    (
      for {
        dashboardRepo <- InMemoryBoardRepo.create[IO](dashboards)
        result        <- IO.pure(
                           BoardMembershipServiceFixtures.withBoardMembershipService(
                             members = members,
                             roles = roles,
                             permissions = permissions
                           ) { membershipCtx =>
                             run(
                               BoardAccessServiceContext(
                                 dashboardRepo = dashboardRepo,
                                 membershipFixtures = membershipCtx,
                                 dashboardAccessService = new BoardAccessServiceLive[IO](
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
