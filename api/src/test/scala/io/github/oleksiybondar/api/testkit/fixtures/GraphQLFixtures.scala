package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.auth.{AccessToken, AuthServiceLive}
import io.github.oleksiybondar.api.domain.user.{User, UserId}
import io.github.oleksiybondar.api.http.middleware.AuthMiddleware
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLRoutes
import io.github.oleksiybondar.api.testkit.support.{InMemoryAuthRepo, InMemoryUserRepo}
import org.http4s.HttpApp
import org.http4s.server.Router

object GraphQLFixtures {

  final case class GraphQLTestContext(
    userRepo: InMemoryUserRepo[IO],
    authRepo: InMemoryAuthRepo[IO],
    httpApp: HttpApp[IO]
  ) {
    def seedAccessToken(token: String, userId: UserId): IO[Unit] =
      authRepo.saveAccessToken(AccessToken(token), userId)
  }

  def withGraphQLRoutes[A](
    users: List[User] = List(UserFixtures.sampleUser)
  )(run: GraphQLTestContext => IO[A]): A =
    (for {
      userRepo <- InMemoryUserRepo.create[IO](users)
      authRepo <- InMemoryAuthRepo.create[IO]()
      authService = AuthServiceLive[IO](userRepo, authRepo)
      graphqlRoutes <- GraphQLRoutes.routes(GraphQLContext(userRepo = userRepo))
      protectedGraphqlRoutes =
        AuthMiddleware.middleware[IO](authService)(graphqlRoutes)
      httpApp =
        Router("/graphql" -> protectedGraphqlRoutes).orNotFound
      result <- run(GraphQLTestContext(userRepo, authRepo, httpApp))
    } yield result).unsafeRunSync()
}
