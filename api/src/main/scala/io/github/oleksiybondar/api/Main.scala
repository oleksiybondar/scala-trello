package io.github.oleksiybondar.api

import cats.effect.{IO, IOApp, Ref, Resource}
import com.comcast.ip4s.{Host, Port}
import io.github.oleksiybondar.api.config.{AppConfig, ConfigLoader}
import io.github.oleksiybondar.api.domain.auth.{AccessToken, RefreshToken}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.http.HttpApi
import io.github.oleksiybondar.api.http.docs.graphql.GraphiQLRoutes
import io.github.oleksiybondar.api.http.docs.rest.OpenAPI
import io.github.oleksiybondar.api.http.middleware.AuthMiddleware
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLRoutes
import io.github.oleksiybondar.api.http.routes.rest.health.HealthRoutes
import io.github.oleksiybondar.api.infrastructure.db.user.SlickUserRepo
import io.github.oleksiybondar.api.modules.AuthModule
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import slick.jdbc.PostgresProfile.api.Database

import scala.concurrent.ExecutionContext

object Main extends IOApp.Simple {

  given ExecutionContext = ExecutionContext.global

  override def run: IO[Unit] =
    loadConfig.flatMap(buildAndRun)

  def loadConfig: IO[AppConfig] =
    IO.fromEither(ConfigLoader.load())

  private def buildAndRun(
                           config: AppConfig
                         ): IO[Unit] =
    for {
      httpApp <- buildApp(config)
      _ <- runServer(config, httpApp)
    } yield ()

  def buildApp(
                        config: AppConfig
                      ): IO[HttpApp[IO]] =
    buildHttpApp(config)

  def buildServer(
                           config: AppConfig,
                           httpApp: HttpApp[IO]
                         ): Resource[IO, Server] =
    Resource.suspend {
      for {
        host <- parseHost(config)
        port <- parsePort(config)
      } yield
        EmberServerBuilder
          .default[IO]
          .withHost(host)
          .withPort(port)
          .withHttpApp(httpApp)
          .build
    }
    
  private def runServer(
                         config: AppConfig,
                         httpApp: HttpApp[IO]
                       ): IO[Unit] =
    buildServer(config, httpApp).useForever

  def buildHttpApp(
                            config: AppConfig
                          ): IO[HttpApp[IO]] =
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
      graphQLContext = GraphQLContext(userRepo = userRepo)

      accessTokenStore <- Ref.of[IO, Map[AccessToken, UserId]](Map.empty)
      refreshTokenStore <- Ref.of[IO, Map[RefreshToken, UserId]](Map.empty)

      authModule = AuthModule.make[IO](
        userRepo,
        accessTokenStore,
        refreshTokenStore
      )

      healthRoutes = HealthRoutes.routes[IO]
      swaggerRoutes = OpenAPI.routes[IO]
      graphqlRoutes <- GraphQLRoutes.routes(graphQLContext)
      graphiqlRoutes = GraphiQLRoutes.routes[IO]

      authenticatedGraphqlRoutes =
        AuthMiddleware.middleware[IO](authModule.authService)(graphqlRoutes)

      httpApp = HttpApi.make[IO](
        healthRoutes,
        authModule.authRoutes,
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
