package io.github.oleksiybondar.api

import cats.effect.{IO, IOApp, Resource}
import com.comcast.ip4s.{Host, Port}
import io.github.oleksiybondar.api.config.{AppConfig, ConfigLoader, DebugMode}
import io.github.oleksiybondar.api.domain.board.{
  BoardAccessService,
  BoardMembershipService,
  BoardService
}
import io.github.oleksiybondar.api.domain.permission.{PermissionService, RoleService}
import io.github.oleksiybondar.api.domain.user.UserService
import io.github.oleksiybondar.api.http.HttpApi
import io.github.oleksiybondar.api.http.docs.graphql.GraphiQLRoutes
import io.github.oleksiybondar.api.http.docs.rest.OpenAPI
import io.github.oleksiybondar.api.http.middleware.{AuthMiddleware, LoggingMiddleware}
import io.github.oleksiybondar.api.http.routes.graphql.{GraphQLContext, GraphQLRoutes}
import io.github.oleksiybondar.api.http.routes.rest.auth.AuthRoutes
import io.github.oleksiybondar.api.http.routes.rest.health.HealthRoutes
import io.github.oleksiybondar.api.infrastructure.db.DatabaseResource
import io.github.oleksiybondar.api.modules.{ApplicationModules, AuthModule}
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
    appResource(config, DebugMode.isEnabled())

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
      config: AppConfig,
      debugEnabled: Boolean
  ): Resource[IO, HttpApp[IO]] =
    for {
      db            <- databaseResource(config)
      modules        = ApplicationModules.make[IO](config, db)
      graphqlRoutes <- graphqlRoutesResource(
                         modules.user.userService,
                         modules.board.boardService,
                         modules.board.boardMembershipService,
                         modules.board.boardAccessService,
                         modules.permission.roleService,
                         modules.permission.permissionService,
                         modules.ticket.ticketService,
                         modules.dictionary.ticketStateRepo,
                         modules.timeTracking.timeTrackingService,
                         modules.comment.commentService,
                         modules.auth.authService
                       )
    } yield {
      buildHttpApp(modules.auth, modules.user.userService, graphqlRoutes, debugEnabled)
    }

  def databaseResource(config: AppConfig): Resource[IO, Database] =
    DatabaseResource.make(config.database)

  def graphqlRoutesResource(
      userService: UserService[IO],
      dashboardService: BoardService[IO],
      dashboardMembershipService: BoardMembershipService[IO],
      dashboardAccessService: BoardAccessService[IO],
      roleService: RoleService[IO],
      permissionService: PermissionService[IO],
      ticketService: io.github.oleksiybondar.api.domain.ticket.TicketService[IO],
      ticketStateRepo: io.github.oleksiybondar.api.infrastructure.db.ticket.TicketStateRepo[IO],
      timeTrackingService: io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingService[IO],
      commentService: io.github.oleksiybondar.api.domain.comment.CommentService[IO],
      authService: io.github.oleksiybondar.api.domain.auth.AuthService[IO]
  ): Resource[IO, HttpRoutes[IO]] =
    Resource.eval(
      GraphQLRoutes.routes(
        GraphQLContext(
          userService = userService,
          dashboardService = dashboardService,
          dashboardMembershipService = dashboardMembershipService,
          dashboardAccessService = dashboardAccessService,
          roleService = roleService,
          permissionService = permissionService,
          ticketService = ticketService,
          ticketStateRepo = ticketStateRepo,
          timeTrackingService = timeTrackingService,
          commentService = commentService,
          authService = authService,
          currentUserId = None
        )
      )
    )

  def buildHttpApp(
      authModule: AuthModule[IO],
      userService: UserService[IO],
      graphqlRoutes: HttpRoutes[IO],
      debugEnabled: Boolean
  ): HttpApp[IO] = {
    val authenticatedGraphqlRoutes =
      AuthMiddleware.middleware[IO](authModule.authService)(graphqlRoutes)

    val httpApp = HttpApi.make[IO](
      HealthRoutes.routes[IO],
      AuthRoutes.routes[IO](authModule.authService, userService),
      authenticatedGraphqlRoutes,
      OpenAPI.routes[IO],
      GraphiQLRoutes.routes[IO]
    )

    LoggingMiddleware[IO](debugEnabled).apply(httpApp)
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
