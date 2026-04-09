package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.board.{Board, BoardMember}
import io.github.oleksiybondar.api.domain.permission.{Permission, Role}
import io.github.oleksiybondar.api.domain.ticket.Ticket
import io.github.oleksiybondar.api.domain.timeTracking.{TimeTrackingEntry, TimeTrackingServiceLive}
import io.github.oleksiybondar.api.testkit.support.{
  InMemoryBoardRepo,
  InMemoryTicketRepo,
  InMemoryTimeTrackingRepo
}

object TimeTrackingServiceFixtures {

  final case class TimeTrackingServiceContext(
      boardRepo: InMemoryBoardRepo[IO],
      ticketRepo: InMemoryTicketRepo[IO],
      timeTrackingRepo: InMemoryTimeTrackingRepo[IO],
      membershipFixtures: BoardMembershipServiceFixtures.BoardMembershipServiceContext,
      timeTrackingService: TimeTrackingServiceLive[IO]
  )

  def withTimeTrackingService[A](
      entries: List[TimeTrackingEntry] = Nil,
      tickets: List[Ticket] = Nil,
      boards: List[Board] = Nil,
      members: List[BoardMember] = Nil,
      roles: List[Role] = Nil,
      permissions: List[Permission] = Nil
  )(run: TimeTrackingServiceContext => IO[A]): A =
    (
      for {
        boardRepo        <- InMemoryBoardRepo.create[IO](boards)
        ticketRepo       <- InMemoryTicketRepo.create[IO](tickets)
        timeTrackingRepo <- InMemoryTimeTrackingRepo.create[IO](entries)
        result           <- IO.pure(
                              BoardMembershipServiceFixtures.withBoardMembershipService(
                                members = members,
                                roles = roles,
                                permissions = permissions
                              ) { membershipCtx =>
                                run(
                                  TimeTrackingServiceContext(
                                    boardRepo = boardRepo,
                                    ticketRepo = ticketRepo,
                                    timeTrackingRepo = timeTrackingRepo,
                                    membershipFixtures = membershipCtx,
                                    timeTrackingService = new TimeTrackingServiceLive[IO](
                                      timeTrackingRepo,
                                      ticketRepo,
                                      boardRepo,
                                      membershipCtx.dashboardMembershipService,
                                      new io.github.oleksiybondar.api.infrastructure.db.timeTracking.TimeTrackingActivityRepo[
                                        IO
                                      ] {
                                        override def findById(
                                            id: io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingActivityId
                                        ): IO[Option[
                                          io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingActivity
                                        ]] =
                                          IO.pure(
                                            List(
                                              TimeTrackingActivityFixtures.codeReviewActivity,
                                              TimeTrackingActivityFixtures.developmentActivity,
                                              TimeTrackingActivityFixtures.testingActivity,
                                              TimeTrackingActivityFixtures.planningActivity,
                                              TimeTrackingActivityFixtures.designActivity,
                                              TimeTrackingActivityFixtures.documentationActivity,
                                              TimeTrackingActivityFixtures.refinementActivity,
                                              TimeTrackingActivityFixtures.debuggingActivity
                                            ).find(_.id == id)
                                          )

                                        override def findByCode(
                                            code: io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingActivityCode
                                        ): IO[Option[
                                          io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingActivity
                                        ]] =
                                          IO.pure(
                                            List(
                                              TimeTrackingActivityFixtures.codeReviewActivity,
                                              TimeTrackingActivityFixtures.developmentActivity,
                                              TimeTrackingActivityFixtures.testingActivity,
                                              TimeTrackingActivityFixtures.planningActivity,
                                              TimeTrackingActivityFixtures.designActivity,
                                              TimeTrackingActivityFixtures.documentationActivity,
                                              TimeTrackingActivityFixtures.refinementActivity,
                                              TimeTrackingActivityFixtures.debuggingActivity
                                            ).find(_.code == code)
                                          )

                                        override def list
                                            : IO[List[
                                              io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingActivity
                                            ]] =
                                          IO.pure(
                                            List(
                                              TimeTrackingActivityFixtures.codeReviewActivity,
                                              TimeTrackingActivityFixtures.developmentActivity,
                                              TimeTrackingActivityFixtures.testingActivity,
                                              TimeTrackingActivityFixtures.planningActivity,
                                              TimeTrackingActivityFixtures.designActivity,
                                              TimeTrackingActivityFixtures.documentationActivity,
                                              TimeTrackingActivityFixtures.refinementActivity,
                                              TimeTrackingActivityFixtures.debuggingActivity
                                            )
                                          )
                                      }
                                    )
                                  )
                                )
                              }
                            )
      } yield result
    ).unsafeRunSync()
}
