package io.github.oleksiybondar.api

import cats.effect.{IO, IOApp, Resource}
import com.comcast.ip4s.{Host, Port}
import io.github.oleksiybondar.api.config.{AppConfig, ConfigLoader}
import io.github.oleksiybondar.api.domain.dashboard.{
  DashboardAccessService,
  DashboardAccessServiceLive,
  DashboardMembershipService,
  DashboardMembershipServiceLive,
  DashboardService,
  DashboardServiceLive
}
import io.github.oleksiybondar.api.domain.permission.{
  PermissionService,
  PermissionServiceLive,
  RoleService,
  RoleServiceLive
}
import io.github.oleksiybondar.api.domain.user.{UserService, UserServiceLive}
import io.github.oleksiybondar.api.http.HttpApi
import io.github.oleksiybondar.api.http.docs.graphql.GraphiQLRoutes
import io.github.oleksiybondar.api.http.docs.rest.OpenAPI
import io.github.oleksiybondar.api.http.middleware.AuthMiddleware
import io.github.oleksiybondar.api.http.routes.graphql.{GraphQLContext, GraphQLRoutes}
import io.github.oleksiybondar.api.http.routes.rest.auth.AuthRoutes
import io.github.oleksiybondar.api.http.routes.rest.health.HealthRoutes
import io.github.oleksiybondar.api.infrastructure.auth.password.{
  PasswordHistoryLive,
  PasswordStrengthValidatorLive
}
import io.github.oleksiybondar.api.infrastructure.crypto.Password4jPasswordHasher
import io.github.oleksiybondar.api.infrastructure.db.DatabaseResource
import io.github.oleksiybondar.api.infrastructure.db.auth.password.{
  PasswordHistoryRepo,
  PasswordHistoryRepoSlick
}
import io.github.oleksiybondar.api.infrastructure.db.auth.{AuthSessionRepo, AuthSessionRepoSlick}
import io.github.oleksiybondar.api.infrastructure.db.dashboard.{
  DashboardMemberRepo,
  DashboardRepo,
  SlickDashboardMemberRepo,
  SlickDashboardRepo
}
import io.github.oleksiybondar.api.infrastructure.db.permission.{
  PermissionRepo,
  RoleRepo,
  SlickPermissionRepo,
  SlickRoleRepo
}
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
      db                        <- databaseResource(config)
      userRepo                   = buildUserRepo(db)
      dashboardRepo              = buildDashboardRepo(db)
      dashboardMemberRepo        = buildDashboardMemberRepo(db)
      authSessionRepo            = buildAuthSessionRepo(db)
      roleRepo                   = buildRoleRepo(db)
      permissionRepo             = buildPermissionRepo(db)
      passwordHistoryRepo        = buildPasswordHistoryRepo(db)
      userService                = buildUserService(config, userRepo, passwordHistoryRepo)
      roleService                = buildRoleService(roleRepo, permissionRepo)
      permissionService          = buildPermissionService(permissionRepo)
      dashboardMembershipService = buildDashboardMembershipService(dashboardMemberRepo, roleService)
      dashboardAccessService     =
        buildDashboardAccessService(dashboardRepo, dashboardMembershipService)
      dashboardService           =
        buildDashboardService(
          dashboardRepo,
          dashboardAccessService,
          dashboardMembershipService,
          roleService
        )
      authModule                 = buildAuthModule(config, userRepo, authSessionRepo, passwordHistoryRepo)
      graphqlRoutes             <- graphqlRoutesResource(
                                     userService,
                                     dashboardService,
                                     dashboardMembershipService,
                                     dashboardAccessService,
                                     roleService,
                                     permissionService,
                                     authModule.authService
                                   )
    } yield {
      buildHttpApp(authModule, userService, graphqlRoutes)
    }

  def databaseResource(config: AppConfig): Resource[IO, Database] =
    DatabaseResource.make(config.database)

  def graphqlRoutesResource(
      userService: UserService[IO],
      dashboardService: DashboardService[IO],
      dashboardMembershipService: DashboardMembershipService[IO],
      dashboardAccessService: DashboardAccessService[IO],
      roleService: RoleService[IO],
      permissionService: PermissionService[IO],
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
          authService = authService,
          currentUserId = None
        )
      )
    )

  def buildUserRepo(db: Database): UserRepo[IO] =
    new SlickUserRepo[IO](db)

  def buildDashboardRepo(db: Database): DashboardRepo[IO] =
    new SlickDashboardRepo[IO](db)

  def buildDashboardMemberRepo(db: Database): DashboardMemberRepo[IO] =
    new SlickDashboardMemberRepo[IO](db)

  def buildRoleRepo(db: Database): RoleRepo[IO] =
    new SlickRoleRepo[IO](db)

  def buildPermissionRepo(db: Database): PermissionRepo[IO] =
    new SlickPermissionRepo[IO](db)

  def buildRoleService(
      roleRepo: RoleRepo[IO],
      permissionRepo: PermissionRepo[IO]
  ): RoleService[IO] =
    new RoleServiceLive[IO](roleRepo, permissionRepo)

  def buildPermissionService(permissionRepo: PermissionRepo[IO]): PermissionService[IO] =
    new PermissionServiceLive[IO](permissionRepo)

  def buildDashboardMembershipService(
      dashboardMemberRepo: DashboardMemberRepo[IO],
      roleService: RoleService[IO]
  ): DashboardMembershipService[IO] =
    new DashboardMembershipServiceLive[IO](dashboardMemberRepo, roleService)

  def buildDashboardAccessService(
      dashboardRepo: DashboardRepo[IO],
      dashboardMembershipService: DashboardMembershipService[IO]
  ): DashboardAccessService[IO] =
    new DashboardAccessServiceLive[IO](dashboardRepo, dashboardMembershipService)

  def buildDashboardService(
      dashboardRepo: DashboardRepo[IO],
      dashboardAccessService: DashboardAccessService[IO],
      dashboardMembershipService: DashboardMembershipService[IO],
      roleService: RoleService[IO]
  ): DashboardService[IO] =
    new DashboardServiceLive[IO](
      dashboardRepo,
      dashboardAccessService,
      dashboardMembershipService,
      roleService
    )

  def buildUserService(
      config: AppConfig,
      userRepo: UserRepo[IO],
      passwordHistoryRepo: PasswordHistoryRepo[IO]
  ): UserService[IO] = {
    val passwordHasher = new Password4jPasswordHasher[IO](config.password)
    new UserServiceLive[IO](
      userRepo,
      passwordHasher,
      new PasswordStrengthValidatorLive(config.password.strength),
      new PasswordHistoryLive[IO](passwordHistoryRepo, passwordHasher, config.password)
    )
  }

  def buildAuthSessionRepo(db: Database): AuthSessionRepo[IO] =
    new AuthSessionRepoSlick[IO](db)

  def buildPasswordHistoryRepo(db: Database): PasswordHistoryRepo[IO] =
    new PasswordHistoryRepoSlick[IO](db)

  def buildAuthModule(
      config: AppConfig,
      userRepo: UserRepo[IO],
      authSessionRepo: AuthSessionRepo[IO],
      passwordHistoryRepo: PasswordHistoryRepo[IO]
  ): AuthModule[IO] =
    AuthModule.make[IO](
      config.auth,
      config.password,
      userRepo,
      authSessionRepo,
      passwordHistoryRepo
    )

  def buildHttpApp(
      authModule: AuthModule[IO],
      userService: UserService[IO],
      graphqlRoutes: HttpRoutes[IO]
  ): HttpApp[IO] = {
    val authenticatedGraphqlRoutes =
      AuthMiddleware.middleware[IO](authModule.authService)(graphqlRoutes)

    HttpApi.make[IO](
      HealthRoutes.routes[IO],
      AuthRoutes.routes[IO](authModule.authService, userService),
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
