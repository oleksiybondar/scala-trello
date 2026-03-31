package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.auth.{AccessTokenClaims, AuthServiceLive, SessionId}
import io.github.oleksiybondar.api.domain.user.{User, UserId}
import io.github.oleksiybondar.api.http.middleware.AuthMiddleware
import io.github.oleksiybondar.api.http.routes.graphql.{GraphQLContext, GraphQLRoutes}
import io.github.oleksiybondar.api.infrastructure.auth.JwtServiceLive
import io.github.oleksiybondar.api.testkit.support.{InMemoryAuthRepo, InMemoryUserRepo}
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
      userRepo              <- InMemoryUserRepo.create[IO](users)
      authRepo              <- InMemoryAuthRepo.create[IO]()
      jwtService             = new JwtServiceLive[IO](AuthServiceFixtures.testAuthConfig.jwtSecret)
      authService            = new AuthServiceLive[IO](
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
      graphqlRoutes         <- GraphQLRoutes.routes(GraphQLContext(userRepo = userRepo))
      protectedGraphqlRoutes =
        AuthMiddleware.middleware[IO](authService)(graphqlRoutes)
      httpApp                =
        Router("/graphql" -> protectedGraphqlRoutes).orNotFound
      result                <- run(GraphQLTestContext(userRepo, jwtService, httpApp))
    } yield result).unsafeRunSync()
}
