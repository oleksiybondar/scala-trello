package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.auth.{AccessTokenClaims, AuthServiceLive, SessionId}
import io.github.oleksiybondar.api.domain.board.{
  BoardAccessServiceLive,
  BoardMembershipServiceLive,
  BoardServiceLive
}
import io.github.oleksiybondar.api.domain.comment.CommentServiceLive
import io.github.oleksiybondar.api.domain.permission.{PermissionServiceLive, RoleServiceLive}
import io.github.oleksiybondar.api.domain.ticket.TicketServiceLive
import io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingServiceLive
import io.github.oleksiybondar.api.domain.user.{User, UserId, UserServiceLive}
import io.github.oleksiybondar.api.http.middleware.AuthMiddleware
import io.github.oleksiybondar.api.http.routes.graphql.{GraphQLContext, GraphQLRoutes}
import io.github.oleksiybondar.api.infrastructure.auth.JwtServiceLive
import io.github.oleksiybondar.api.testkit.support.{
  InMemoryAuthRepo,
  InMemoryBoardMemberRepo,
  InMemoryBoardRepo,
  InMemoryCommentQueryRepo,
  InMemoryCommentRepo,
  InMemoryPermissionRepo,
  InMemoryRoleQueryRepo,
  InMemoryRoleRepo,
  InMemoryTicketRepo,
  InMemoryTicketStateRepo,
  InMemoryTimeTrackingActivityRepo,
  InMemoryTimeTrackingQueryRepo,
  InMemoryTimeTrackingRepo,
  InMemoryUserRepo
}
import org.http4s.HttpApp
import org.http4s.server.Router

object GraphQLFixtures {

  final case class GraphQLTestContext(
      userRepo: InMemoryUserRepo[IO],
      jwtService: JwtServiceLive[IO],
      httpApp: HttpApp[IO]
  ) {
    def issueAccessToken(userId: UserId): IO[String] =
      for {
        now   <- IO.realTimeInstant
        token <- jwtService.encode(
                   AccessTokenClaims(
                     userId = userId,
                     sessionId = SessionId(java.util.UUID.randomUUID()),
                     tokenId = java.util.UUID.randomUUID(),
                     issuedAt = now,
                     expiresAt =
                       now.plusSeconds(AuthServiceFixtures.testAuthConfig.accessTokenTtlSeconds)
                   )
                 )
      } yield token.value
  }

  def withGraphQLRoutes[A](
      users: List[User] = List(UserFixtures.sampleUser),
      dashboards: List[io.github.oleksiybondar.api.domain.board.Board] = List(
        BoardFixtures.sampleDashboard
      ),
      members: List[io.github.oleksiybondar.api.domain.board.BoardMember] = List(
        BoardMemberFixtures.sampleMember,
        BoardMemberFixtures.member(
          userId = io.github.oleksiybondar.api.domain.user.UserId(
            java.util.UUID.fromString("22222222-2222-2222-2222-222222222222")
          ),
          roleId = io.github.oleksiybondar.api.domain.permission.RoleId(2),
          createdAt = java.time.Instant.parse("2026-04-06T08:05:00Z")
        )
      ),
      tickets: List[io.github.oleksiybondar.api.domain.ticket.Ticket] = Nil,
      timeEntries: List[io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingEntry] = Nil,
      comments: List[io.github.oleksiybondar.api.domain.comment.Comment] = Nil
  )(run: GraphQLTestContext => IO[A]): A =
    (for {
      userRepo                  <- InMemoryUserRepo.create[IO](users)
      dashboardRepo             <- InMemoryBoardRepo.create[IO](dashboards)
      dashboardMemberRepo       <- InMemoryBoardMemberRepo.create[IO](members)
      ticketRepo                <- InMemoryTicketRepo.create[IO](tickets)
      commentRepo               <- InMemoryCommentRepo.create[IO](comments)
      timeTrackingRepo          <- InMemoryTimeTrackingRepo.create[IO](timeEntries)
      commentQueryRepo           = new InMemoryCommentQueryRepo[IO](comments, tickets, users)
      timeTrackingQueryRepo      = new InMemoryTimeTrackingQueryRepo[IO](timeEntries, tickets, users)
      timeTrackingActivityRepo   = new InMemoryTimeTrackingActivityRepo[IO](
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
      ticketStateRepo           <- InMemoryTicketStateRepo.create[IO](
                                     List(
                                       TicketStateFixtures.newState,
                                       TicketStateFixtures.inProgressState,
                                       TicketStateFixtures.codeReviewState,
                                       TicketStateFixtures.inTestingState,
                                       TicketStateFixtures.doneState
                                     )
                                   )
      authRepo                  <- InMemoryAuthRepo.create[IO]()
      roleRepo                  <- InMemoryRoleRepo.create[IO](
                                     List(
                                       RoleFixtures.adminRole,
                                       RoleFixtures.contributorRole,
                                       RoleFixtures.viewerRole
                                     )
                                   )
      permissionRepo            <- InMemoryPermissionRepo.create[IO](
                                     List(
                                       PermissionFixtures.adminDashboardPermission,
                                       PermissionFixtures.adminTicketPermission,
                                       PermissionFixtures.adminCommentPermission,
                                       PermissionFixtures.contributorDashboardPermission,
                                       PermissionFixtures.contributorTicketPermission,
                                       PermissionFixtures.contributorCommentPermission,
                                       PermissionFixtures.viewerDashboardPermission,
                                       PermissionFixtures.viewerTicketPermission,
                                       PermissionFixtures.viewerCommentPermission
                                     )
                                   )
      roleQueryRepo              = new InMemoryRoleQueryRepo[IO](
                                     List(
                                       RoleFixtures.adminRole,
                                       RoleFixtures.contributorRole,
                                       RoleFixtures.viewerRole
                                     ),
                                     List(
                                       PermissionFixtures.adminDashboardPermission,
                                       PermissionFixtures.adminTicketPermission,
                                       PermissionFixtures.adminCommentPermission,
                                       PermissionFixtures.contributorDashboardPermission,
                                       PermissionFixtures.contributorTicketPermission,
                                       PermissionFixtures.contributorCommentPermission,
                                       PermissionFixtures.viewerDashboardPermission,
                                       PermissionFixtures.viewerTicketPermission,
                                       PermissionFixtures.viewerCommentPermission
                                     )
                                   )
      jwtService                 = new JwtServiceLive[IO](AuthServiceFixtures.testAuthConfig.jwtSecret)
      authService                = new AuthServiceLive[IO](
                                     userRepo,
                                     authRepo,
                                     jwtService,
                                     AuthServiceFixtures.fakePasswordHasher,
                                     new io.github.oleksiybondar.api.infrastructure.auth.password.PasswordStrengthValidatorLive(
                                       io.github.oleksiybondar.api.config.PasswordStrengthConfig(
                                         minLength = 8,
                                         requireDigit = false,
                                         requireSpecialChar = false
                                       )
                                     ),
                                     AuthServiceFixtures.unsafeEmptyPasswordHistory,
                                     accessTokenTtlSeconds =
                                       AuthServiceFixtures.testAuthConfig.accessTokenTtlSeconds,
                                     sessionTtlDays = AuthServiceFixtures.testAuthConfig.sessionTtlDays
                                   )
      userService                = new UserServiceLive[IO](
                                     userRepo,
                                     AuthServiceFixtures.fakePasswordHasher,
                                     AuthServiceFixtures.passwordStrengthValidator,
                                     AuthServiceFixtures.unsafeEmptyPasswordHistory
                                   )
      roleService                = new RoleServiceLive[IO](roleRepo, permissionRepo)
      permissionService          = new PermissionServiceLive[IO](permissionRepo)
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
      ticketService              = new TicketServiceLive[IO](
                                     ticketRepo,
                                     dashboardAccessService,
                                     dashboardMembershipService
                                   )
      timeTrackingService        = new TimeTrackingServiceLive[IO](
                                     timeTrackingRepo,
                                     ticketRepo,
                                     dashboardRepo,
                                     dashboardAccessService,
                                     dashboardMembershipService,
                                     timeTrackingActivityRepo
                                   )
      commentService             = new CommentServiceLive[IO](
                                     commentRepo,
                                     ticketRepo,
                                     dashboardRepo,
                                     dashboardAccessService
                                   )
      graphqlRoutes             <- GraphQLRoutes.routes(
                                     GraphQLContext(
                                       userService = userService,
                                       dashboardService = dashboardService,
                                       dashboardMembershipService = dashboardMembershipService,
                                       dashboardAccessService = dashboardAccessService,
                                       roleService = roleService,
                                       roleQueryRepo = roleQueryRepo,
                                       permissionService = permissionService,
                                       ticketService = ticketService,
                                       ticketStateRepo = ticketStateRepo,
                                       timeTrackingService = timeTrackingService,
                                       commentQueryRepo = commentQueryRepo,
                                       timeTrackingQueryRepo = timeTrackingQueryRepo,
                                       commentService = commentService,
                                       authService = authService,
                                       currentUserId = None
                                     )
                                   )
      protectedGraphqlRoutes     =
        AuthMiddleware.middleware[IO](authService)(graphqlRoutes)
      httpApp                    =
        Router("/graphql" -> protectedGraphqlRoutes).orNotFound
      result                    <- run(GraphQLTestContext(userRepo, jwtService, httpApp))
    } yield result).unsafeRunSync()
}
