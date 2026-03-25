package io.github.oleksiybondar.api

import cats.effect.{IO, Ref, Resource}
import cats.effect.std.Dispatcher
import com.comcast.ip4s.{Host, Port}
import io.github.oleksiybondar.api.config.AppConfig
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
import org.http4s.server.Server
import slick.jdbc.PostgresProfile.api.Database
import zio.Runtime

import scala.concurrent.ExecutionContext

object AppServer {

  private given ExecutionContext = ExecutionContext.global

  def resource(
    config: AppConfig
  )(using dispatcher: Dispatcher[IO], runtime: Runtime[Any]): Resource[IO, Server] =
    for {
      httpApp <- Resource.eval(buildHttpApp(config))
      host <- Resource.eval(parseHost(config))
      port <- Resource.eval(parsePort(config))
      server <-
        EmberServerBuilder
          .default[IO]
          .withHost(host)
          .withPort(port)
          .withHttpApp(httpApp)
          .build
    } yield server

  private def buildHttpApp(
    config: AppConfig
  )(using Dispatcher[IO], Runtime[Any]): IO[HttpApp[IO]] =
    databaseResource(config).use { db =>
      val userRepo = new SlickUserRepo[IO](db)

      for {
        accessTokenStore <- Ref.of[IO, Map[AccessToken, UserId]](Map.empty)
        refreshTokenStore <- Ref.of[IO, Map[RefreshToken, UserId]](Map.empty)
        graphqlRoutes <- GraphQLRoutes.routes(userRepo)
      } yield HttpApi.make[IO](
        HealthRoutes.routes[IO],
        AuthRoutes.routes[IO](
          new AuthServiceLive[IO](
            userRepo,
            accessTokenStore,
            refreshTokenStore
          )
        ),
        AuthMiddleware.middleware[IO](accessTokenStore)(graphqlRoutes),
        OpenAPI.routes[IO],
        GraphiQLRoutes.routes[IO]
      )
    }

  private def databaseResource(config: AppConfig): Resource[IO, Database] =
    Resource.make {
      IO(
        Database.forURL(
          url = config.database.db.url,
          user = config.database.db.user,
          password = config.database.db.password,
          driver = config.database.db.driver
        )
      )
    } { db =>
      IO(db.close()).handleError(_ => ())
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
