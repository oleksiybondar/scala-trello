package io.github.oleksiybondar.api.http.routes.graphql.dashboard

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.dashboard.{
  Dashboard,
  DashboardDescription,
  DashboardId,
  DashboardName
}
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import io.github.oleksiybondar.api.http.routes.graphql.user.UserApi.InvalidUserInput
import sangria.execution.UserFacingError
import sangria.schema.{
  Argument,
  BooleanType,
  Field,
  ListType,
  LongType,
  ObjectType,
  OptionInputType,
  StringType,
  fields
}

import java.util.UUID
import scala.util.Try

object DashboardApi {

  final case class DashboardAccessDenied(override val getMessage: String)
      extends IllegalArgumentException(getMessage)
      with UserFacingError

  private val DashboardIdArg = Argument("dashboardId", StringType)
  private val NameArg        = Argument("name", StringType)
  private val DescriptionArg = Argument("description", OptionInputType(StringType))
  private val UserIdArg      = Argument("userId", StringType)
  private val RoleIdArg      = Argument("roleId", LongType)

  val DashboardRoleType: ObjectType[Unit, DashboardRoleView] =
    ObjectType(
      name = "DashboardRoleView",
      fields[Unit, DashboardRoleView](
        Field("id", StringType, resolve = _.value.id),
        Field("name", StringType, resolve = _.value.name),
        Field(
          "description",
          sangria.schema.OptionType(StringType),
          resolve = _.value.description
        )
      )
    )

  val DashboardType: ObjectType[Unit, DashboardView] =
    ObjectType(
      name = "DashboardView",
      fields[Unit, DashboardView](
        Field("id", StringType, resolve = _.value.id),
        Field("name", StringType, resolve = _.value.name),
        Field(
          "description",
          sangria.schema.OptionType(StringType),
          resolve = _.value.description
        ),
        Field("active", sangria.schema.BooleanType, resolve = _.value.active),
        Field("ownerUserId", StringType, resolve = _.value.ownerUserId),
        Field("createdByUserId", StringType, resolve = _.value.createdByUserId),
        Field("createdAt", StringType, resolve = _.value.createdAt),
        Field("modifiedAt", StringType, resolve = _.value.modifiedAt),
        Field("lastModifiedByUserId", StringType, resolve = _.value.lastModifiedByUserId)
      )
    )

  val DashboardMemberType: ObjectType[Unit, DashboardMemberView] =
    ObjectType(
      name = "DashboardMemberView",
      fields[Unit, DashboardMemberView](
        Field("dashboardId", StringType, resolve = _.value.dashboardId),
        Field("userId", StringType, resolve = _.value.userId),
        Field("createdAt", StringType, resolve = _.value.createdAt),
        Field("role", DashboardRoleType, resolve = _.value.role)
      )
    )

  val queryFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "myDashboards",
        fieldType = ListType(DashboardType),
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            ctx.ctx.dashboardService
              .listDashboardsForUser(currentUserId)
              .map(_.map(toDashboardView))
          }.unsafeToFuture()
      ),
      Field(
        name = "dashboardMembers",
        fieldType = ListType(DashboardMemberType),
        arguments = DashboardIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId <- IO.fromEither(parseDashboardId(ctx.arg(DashboardIdArg)))
              canRead     <- ctx.ctx.dashboardAccessService.canReadDashboard(dashboardId, currentUserId)
              _           <-
                if (canRead) IO.unit
                else
                  IO.raiseError(DashboardAccessDenied("You do not have access to this dashboard"))
              members     <- ctx.ctx.dashboardMembershipService.listMembers(dashboardId)
            } yield members.map(toMemberView)
          }.unsafeToFuture()
      )
    )

  val mutationFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "createDashboard",
        fieldType = DashboardType,
        arguments = NameArg :: DescriptionArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              now      <- IO.realTimeInstant
              dashboard = Dashboard(
                            id = DashboardId(UUID.randomUUID()),
                            name = DashboardName(ctx.arg(NameArg).trim),
                            description = normalizeDescription(ctx.arg(DescriptionArg)),
                            active = true,
                            ownerUserId = currentUserId,
                            createdByUserId = currentUserId,
                            createdAt = now,
                            modifiedAt = now,
                            lastModifiedByUserId = currentUserId
                          )
              _        <- ctx.ctx.dashboardService.createDashboard(dashboard)
            } yield toDashboardView(dashboard)
          }.unsafeToFuture()
      ),
      Field(
        name = "deactivateDashboard",
        fieldType = DashboardType,
        arguments = DashboardIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId <- IO.fromEither(parseDashboardId(ctx.arg(DashboardIdArg)))
              updated     <- ctx.ctx.dashboardService.deactivate(dashboardId, currentUserId)
              _           <-
                if (updated) IO.unit
                else IO.raiseError(DashboardAccessDenied("Dashboard could not be deactivated"))
              dashboard   <- ctx.ctx.dashboardService.getDashboard(dashboardId).flatMap(
                               _.liftTo[IO](InvalidUserInput("Dashboard was not found"))
                             )
            } yield toDashboardView(dashboard)
          }.unsafeToFuture()
      ),
      Field(
        name = "inviteDashboardMember",
        fieldType = DashboardMemberType,
        arguments = DashboardIdArg :: UserIdArg :: RoleIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseDashboardId(ctx.arg(DashboardIdArg)))
              memberUserId <- IO.fromEither(parseUserId(ctx.arg(UserIdArg)))
              changed      <- ctx.ctx.dashboardService.addMember(
                                dashboardId,
                                currentUserId,
                                memberUserId,
                                RoleId(ctx.arg(RoleIdArg))
                              )
              _            <-
                if (changed) IO.unit
                else
                  IO.raiseError(DashboardAccessDenied("Member could not be added to the dashboard"))
              member       <-
                ctx.ctx.dashboardMembershipService.findMember(dashboardId, memberUserId).flatMap(
                  _.liftTo[IO](InvalidUserInput("Dashboard member was not found"))
                )
            } yield toMemberView(member)
          }.unsafeToFuture()
      ),
      Field(
        name = "changeDashboardMemberRole",
        fieldType = DashboardMemberType,
        arguments = DashboardIdArg :: UserIdArg :: RoleIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseDashboardId(ctx.arg(DashboardIdArg)))
              memberUserId <- IO.fromEither(parseUserId(ctx.arg(UserIdArg)))
              changed      <- ctx.ctx.dashboardService.changeMemberRole(
                                dashboardId,
                                currentUserId,
                                memberUserId,
                                RoleId(ctx.arg(RoleIdArg))
                              )
              _            <-
                if (changed) IO.unit
                else IO.raiseError(DashboardAccessDenied("Member role could not be changed"))
              member       <-
                ctx.ctx.dashboardMembershipService.findMember(dashboardId, memberUserId).flatMap(
                  _.liftTo[IO](InvalidUserInput("Dashboard member was not found"))
                )
            } yield toMemberView(member)
          }.unsafeToFuture()
      ),
      Field(
        name = "removeDashboardMember",
        fieldType = BooleanType,
        arguments = DashboardIdArg :: UserIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseDashboardId(ctx.arg(DashboardIdArg)))
              memberUserId <- IO.fromEither(parseUserId(ctx.arg(UserIdArg)))
              removed      <-
                ctx.ctx.dashboardService.removeMember(dashboardId, currentUserId, memberUserId)
            } yield removed
          }.unsafeToFuture()
      )
    )

  private def withCurrentUser[A](
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  )(run: io.github.oleksiybondar.api.domain.user.UserId => IO[A]): IO[A] =
    ctx.ctx.currentUserId match {
      case Some(userId) => run(userId)
      case None         => IO.raiseError(InvalidUserInput("Authentication context is missing"))
    }

  private def parseDashboardId(rawId: String): Either[InvalidUserInput, DashboardId] =
    Try(UUID.fromString(rawId))
      .toEither
      .left
      .map(_ => InvalidUserInput(s"Invalid UUID: $rawId"))
      .map(DashboardId(_))

  private def parseUserId(rawId: String)
      : Either[InvalidUserInput, io.github.oleksiybondar.api.domain.user.UserId] =
    Try(UUID.fromString(rawId))
      .toEither
      .left
      .map(_ => InvalidUserInput(s"Invalid UUID: $rawId"))
      .map(io.github.oleksiybondar.api.domain.user.UserId(_))

  private def normalizeDescription(rawDescription: Option[String]): Option[DashboardDescription] =
    rawDescription.map(_.trim).filter(_.nonEmpty).map(DashboardDescription(_))

  private def toMemberView(
      member: io.github.oleksiybondar.api.domain.dashboard.DashboardMemberWithRole
  ) =
    DashboardMemberView(
      dashboardId = member.member.dashboardId.value.toString,
      userId = member.member.userId.value.toString,
      createdAt = member.member.createdAt.toString,
      role = DashboardRoleView(
        id = member.role.role.id.value.toString,
        name = member.role.role.name.value,
        description = member.role.role.description
      )
    )

  private def toDashboardView(dashboard: io.github.oleksiybondar.api.domain.dashboard.Dashboard) =
    DashboardView(
      id = dashboard.id.value.toString,
      name = dashboard.name.value,
      description = dashboard.description.map(_.value),
      active = dashboard.active,
      ownerUserId = dashboard.ownerUserId.value.toString,
      createdByUserId = dashboard.createdByUserId.value.toString,
      createdAt = dashboard.createdAt.toString,
      modifiedAt = dashboard.modifiedAt.toString,
      lastModifiedByUserId = dashboard.lastModifiedByUserId.value.toString
    )
}
