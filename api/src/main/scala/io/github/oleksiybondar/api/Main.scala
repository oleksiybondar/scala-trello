package io.github.oleksiybondar.api

import cats.effect.{IO, IOApp, Ref}
import cats.effect.std.Dispatcher
import com.comcast.ip4s.{Host, Port}
import io.github.oleksiybondar.api.config.{AppConfig, ConfigLoader}
import io.github.oleksiybondar.api.domain.auth.{AccessToken, AuthServiceLive, RefreshToken}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.http.HttpApi
import io.github.oleksiybondar.api.http.docs.graphql.GraphiQLRoutes
import io.github.oleksiybondar.api.http.docs.rest.OpenAPI
import io.github.oleksiybondar.api.http.middleware.AuthMiddleware
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLRoutes
import io.github.oleksiybondar.api.http.routes.rest.auth.AuthRoutes
import io.github.oleksiybondar.api.http.routes.rest.health.HealthRoutes
import io.github.oleksiybondar.api.infrastructure.db.user.SlickUserRepo
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import slick.jdbc.PostgresProfile.api.Database
import zio.Runtime

import scala.concurrent.ExecutionContext

object Main extends IOApp.Simple {

  given ExecutionContext = ExecutionContext.global
  given Runtime[Any] = Runtime.default

  override def run: IO[Unit] =
    IO.fromEither(ConfigLoader.load()).flatMap { config =>
      Dispatcher.parallel[IO].use { implicit dispatcher =>
        runServer(config)
      }
    }

  private def runServer(
                         config: AppConfig
                       )(using Dispatcher[IO]): IO[Unit] =
    for {
      httpApp <- buildHttpApp(config)
      host <- parseHost(config)
      port <- parsePort(config)
      _ <-
        EmberServerBuilder
          .default[IO]
          .withHost(host)
          .withPort(port)
          .withHttpApp(httpApp)
          .build
          .useForever
    } yield ()

  private def buildHttpApp(
                            config: AppConfig
                          )(using Dispatcher[IO]): IO[HttpApp[IO]] =
    for {
      db <- IO(
        Database.forURL(
          url = config.database.db.url,
          user = config.database.db.user,
          password = config.database.db.password,
          driver = config.database.db.driver
        )
      )

      userRepo = new SlickUserRepo[IO](db)

      accessTokenStore <- Ref.of[IO, Map[AccessToken, UserId]](Map.empty)
      refreshTokenStore <- Ref.of[IO, Map[RefreshToken, UserId]](Map.empty)

      authService = new AuthServiceLive[IO](
        userRepo,
        accessTokenStore,
        refreshTokenStore
      )

      healthRoutes = HealthRoutes.routes[IO]
      authRoutes = AuthRoutes.routes[IO](authService)
      swaggerRoutes = OpenAPI.routes[IO]
      graphqlRoutes <- GraphQLRoutes.routes(userRepo)
      graphiqlRoutes = GraphiQLRoutes.routes[IO]

      authenticatedGraphqlRoutes =
        AuthMiddleware.middleware[IO](accessTokenStore)(graphqlRoutes)

      httpApp = HttpApi.make[IO](
        healthRoutes,
        authRoutes,
        authenticatedGraphqlRoutes,
        swaggerRoutes,
        graphiqlRoutes
      )
    } yield httpApp

  private def parseHost(config: AppConfig): IO[Host] =
    IO.fromOption(Host.fromString(config.http.host))(
      new RuntimeException(s"Invalid host: ${config.http.host}")
    )

  private def parsePort(config: AppConfig): IO[Port] =
    IO.fromOption(Port.fromInt(config.http.port))(
      new RuntimeException(s"Invalid port: ${config.http.port}")
    )
}