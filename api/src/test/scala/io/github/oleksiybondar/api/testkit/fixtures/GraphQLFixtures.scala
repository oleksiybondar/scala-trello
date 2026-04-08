package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.auth.{AccessTokenClaims, AuthServiceLive, SessionId}
import io.github.oleksiybondar.api.domain.board.{
  BoardAccessServiceLive,
  BoardMembershipServiceLive,
  BoardServiceLive
}
import io.github.oleksiybondar.api.domain.permission.{PermissionServiceLive, RoleServiceLive}
import io.github.oleksiybondar.api.domain.user.{User, UserId, UserServiceLive}
import io.github.oleksiybondar.api.http.middleware.AuthMiddleware
import io.github.oleksiybondar.api.http.routes.graphql.{GraphQLContext, GraphQLRoutes}
import io.github.oleksiybondar.api.infrastructure.auth.JwtServiceLive
import io.github.oleksiybondar.api.testkit.support.{
  InMemoryAuthRepo,
  InMemoryBoardMemberRepo,
  InMemoryBoardRepo,
  InMemoryPermissionRepo,
  InMemoryRoleRepo,
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
      users: List[User] = List(UserFixtures.sampleUser)
  )(run: GraphQLTestContext => IO[A]): A =
    (for {
      userRepo                  <- InMemoryUserRepo.create[IO](users)
      dashboardRepo             <- InMemoryBoardRepo.create[IO](List(BoardFixtures.sampleDashboard))
      dashboardMemberRepo       <- InMemoryBoardMemberRepo.create[IO](
                                     List(
                                       BoardMemberFixtures.sampleMember,
                                       BoardMemberFixtures.member(
                                         userId = io.github.oleksiybondar.api.domain.user.UserId(
                                           java.util.UUID.fromString(
                                             "22222222-2222-2222-2222-222222222222"
                                           )
                                         ),
                                         roleId = io.github.oleksiybondar.api.domain.permission.RoleId(2),
                                         createdAt = java.time.Instant.parse("2026-04-06T08:05:00Z")
                                       )
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
      graphqlRoutes             <- GraphQLRoutes.routes(
                                     GraphQLContext(
                                       userService = userService,
                                       dashboardService = dashboardService,
                                       dashboardMembershipService = dashboardMembershipService,
                                       dashboardAccessService = dashboardAccessService,
                                       roleService = roleService,
                                       permissionService = permissionService,
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
