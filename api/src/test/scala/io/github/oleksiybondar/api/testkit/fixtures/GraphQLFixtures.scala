package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.auth.AccessToken
import io.github.oleksiybondar.api.domain.user.{User, UserId}
import io.github.oleksiybondar.api.http.middleware.AuthMiddleware
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLRoutes
import io.github.oleksiybondar.api.testkit.support.{InMemoryAuthRepo, InMemoryUserRepo}
import org.http4s.HttpApp
import org.http4s.server.Router
import zio.Runtime

object GraphQLFixtures {

  final case class GraphQLContext(
    userRepo: InMemoryUserRepo[IO],
    authRepo: InMemoryAuthRepo[IO],
    httpApp: HttpApp[IO]
  ) {
    def seedAccessToken(token: String, userId: UserId): IO[Unit] =
      authRepo.saveAccessToken(AccessToken(token), userId)
  }

  def withGraphQLRoutes[A](
    users: List[User] = List(UserFixtures.sampleUser)
  )(run: GraphQLContext => IO[A]): A =
    Dispatcher.parallel[IO].use { implicit dispatcher =>
      given Runtime[Any] = Runtime.default

      for {
        userRepo <- InMemoryUserRepo.create[IO](users)
        authRepo <- InMemoryAuthRepo.create[IO]()
        graphqlRoutes <- GraphQLRoutes.routes(userRepo)
        protectedGraphqlRoutes =
          AuthMiddleware.middleware[IO](authRepo.accessTokens)(graphqlRoutes)
        httpApp =
          Router("/graphql" -> protectedGraphqlRoutes).orNotFound
        result <- run(GraphQLContext(userRepo, authRepo, httpApp))
      } yield result
    }.unsafeRunSync()
}
