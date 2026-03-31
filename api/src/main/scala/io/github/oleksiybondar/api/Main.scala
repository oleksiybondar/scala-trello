package io.github.oleksiybondar.api

import cats.effect.{IO, IOApp, Resource}
import com.comcast.ip4s.{Host, Port}
import io.github.oleksiybondar.api.config.{AppConfig, ConfigLoader}
import io.github.oleksiybondar.api.http.HttpApi
import io.github.oleksiybondar.api.http.docs.graphql.GraphiQLRoutes
import io.github.oleksiybondar.api.http.docs.rest.OpenAPI
import io.github.oleksiybondar.api.http.middleware.AuthMiddleware
import io.github.oleksiybondar.api.http.routes.graphql.{GraphQLContext, GraphQLRoutes}
import io.github.oleksiybondar.api.http.routes.rest.health.HealthRoutes
import io.github.oleksiybondar.api.infrastructure.db.DatabaseResource
import io.github.oleksiybondar.api.infrastructure.db.auth.{AuthSessionRepo, AuthSessionRepoSlick}
import io.github.oleksiybondar.api.infrastructure.db.user.{SlickUserRepo, UserRepo}
import io.github.oleksiybondar.api.modules.AuthModule
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.{HttpApp, HttpRoutes}
import slick.jdbc.PostgresProfile.api.Database

import scala.concurrent.ExecutionContext

object Main extends IOApp.Simple {

  given ExecutionContext = ExecutionContext.global

  override def run: IO[Unit] =
    loadConfig.flatMap(config => serverResource(config).useForever)

  def loadConfig: IO[AppConfig] =
    IO.fromEither(ConfigLoader.load())

  def serverResource(
      config: AppConfig
  ): Resource[IO, Server] =
    for {
      httpApp <- buildApp(config)
      server  <- buildServer(config, httpApp)
    } yield server

  def buildApp(
      config: AppConfig
  ): Resource[IO, HttpApp[IO]] =
    appResource(config)

  def buildServer(
      config: AppConfig,
      httpApp: HttpApp[IO]
  ): Resource[IO, Server] =
    Resource.suspend {
      for {
        host <- parseHost(config)
        port <- parsePort(config)
      } yield EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(httpApp)
        .build
    }

  def appResource(
      config: AppConfig
  ): Resource[IO, HttpApp[IO]] =
    for {
      db             <- databaseResource(config)
      userRepo        = buildUserRepo(db)
      authSessionRepo = buildAuthSessionRepo(db)
      graphqlRoutes  <- graphqlRoutesResource(userRepo)
    } yield {
      val authModule = buildAuthModule(config, userRepo, authSessionRepo)
      buildHttpApp(authModule, graphqlRoutes)
    }

  def databaseResource(config: AppConfig): Resource[IO, Database] =
    DatabaseResource.make(config.database)

  def graphqlRoutesResource(userRepo: UserRepo[IO]): Resource[IO, HttpRoutes[IO]] =
    Resource.eval(GraphQLRoutes.routes(GraphQLContext(userRepo = userRepo)))

  def buildUserRepo(db: Database): UserRepo[IO] =
    new SlickUserRepo[IO](db)

  def buildAuthSessionRepo(db: Database): AuthSessionRepo[IO] =
    new AuthSessionRepoSlick[IO](db)

  def buildAuthModule(
      config: AppConfig,
      userRepo: UserRepo[IO],
      authSessionRepo: AuthSessionRepo[IO]
  ): AuthModule[IO] =
    AuthModule.make[IO](
      config.auth,
      userRepo,
      authSessionRepo
    )

  def buildHttpApp(
      authModule: AuthModule[IO],
      graphqlRoutes: HttpRoutes[IO]
  ): HttpApp[IO] = {
    val authenticatedGraphqlRoutes =
      AuthMiddleware.middleware[IO](authModule.authService)(graphqlRoutes)

    HttpApi.make[IO](
      HealthRoutes.routes[IO],
      authModule.authRoutes,
      authenticatedGraphqlRoutes,
      OpenAPI.routes[IO],
      GraphiQLRoutes.routes[IO]
    )
  }

  private def parseHost(config: AppConfig): IO[Host] =
    IO.fromOption(Host.fromString(config.http.host))(
      new RuntimeException(s"Invalid host: ${config.http.host}")
    )

  private def parsePort(config: AppConfig): IO[Port] =
    IO.fromOption(Port.fromInt(config.http.port))(
      new RuntimeException(s"Invalid port: ${config.http.port}")
    )
}
