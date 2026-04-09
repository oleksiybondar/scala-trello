package io.github.oleksiybondar.api.http.routes.graphql.board

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.{
  Board,
  BoardDescription,
  BoardId,
  BoardName,
  BoardQueryFilters
}
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import io.github.oleksiybondar.api.http.routes.graphql.permission.RoleApi.PermissionType
import io.github.oleksiybondar.api.http.routes.graphql.user.UserApi.InvalidUserInput
import io.github.oleksiybondar.api.http.routes.graphql.user.{UserApi, UserView}
import sangria.execution.UserFacingError
import sangria.schema.{
  Argument,
  BooleanType,
  Field,
  IntType,
  ListType,
  LongType,
  ObjectType,
  OptionInputType,
  StringType,
  fields
}

import java.util.UUID
import scala.util.Try

object BoardApi {

  final case class BoardAccessDenied(override val getMessage: String)
      extends IllegalArgumentException(getMessage)
      with UserFacingError

  private val BoardIdArg     = Argument("dashboardId", StringType)
  private val KeywordArg     = Argument("keyword", OptionInputType(StringType))
  private val NameArg        = Argument("name", StringType)
  private val OwnerUserIdArg = Argument("ownerUserId", OptionInputType(StringType))
  private val DescriptionArg = Argument("description", OptionInputType(StringType))
  private val UserIdArg      = Argument("userId", StringType)
  private val RoleIdArg      = Argument("roleId", LongType)

  val BoardRoleType: ObjectType[GraphQLContext, BoardRoleView] =
    ObjectType(
      name = "BoardRoleView",
      fields[GraphQLContext, BoardRoleView](
        Field("id", StringType, resolve = _.value.id),
        Field("name", StringType, resolve = _.value.name),
        Field(
          "description",
          sangria.schema.OptionType(StringType),
          resolve = _.value.description
        ),
        Field(
          "permissions",
          ListType(PermissionType),
          resolve =
            ctx =>
              ctx.ctx.roleService
                .getRoleWithPermissions(RoleId(ctx.value.id.toLong))
                .map(_.fold(List.empty)(_.permissions))
                .unsafeToFuture()
        )
      )
    )

  val BoardType: ObjectType[GraphQLContext, BoardView] =
    ObjectType(
      name = "BoardView",
      fields[GraphQLContext, BoardView](
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
        Field("lastModifiedByUserId", StringType, resolve = _.value.lastModifiedByUserId),
        Field(
          "owner",
          sangria.schema.OptionType(UserApi.UserType),
          resolve = ctx => loadUserView(ctx.ctx, ctx.value.ownerUserId).unsafeToFuture()
        ),
        Field(
          "createdBy",
          sangria.schema.OptionType(UserApi.UserType),
          resolve = ctx => loadUserView(ctx.ctx, ctx.value.createdByUserId).unsafeToFuture()
        ),
        Field(
          "lastModifiedBy",
          sangria.schema.OptionType(UserApi.UserType),
          resolve = ctx => loadUserView(ctx.ctx, ctx.value.lastModifiedByUserId).unsafeToFuture()
        ),
        Field(
          "membersCount",
          IntType,
          resolve =
            ctx =>
              ctx.ctx.dashboardMembershipService
                .listMembers(BoardId(UUID.fromString(ctx.value.id)))
                .map(_.size)
                .unsafeToFuture()
        )
      )
    )

  val BoardMemberType: ObjectType[GraphQLContext, BoardMemberView] =
    ObjectType(
      name = "BoardMemberView",
      fields[GraphQLContext, BoardMemberView](
        Field("boardId", StringType, resolve = _.value.boardId),
        // TODO: remove this legacy alias after the UI migrates to `boardId`.
        Field("dashboardId", StringType, resolve = _.value.boardId),
        Field("userId", StringType, resolve = _.value.userId),
        Field("createdAt", StringType, resolve = _.value.createdAt),
        Field("role", BoardRoleType, resolve = _.value.role)
      )
    )

  val queryFields: List[Field[GraphQLContext, Unit]] =
    List(
      boardListField("myBoards"),
      // TODO: remove this legacy alias after the UI migrates to `myBoards`.
      boardListField("myDashboards"),
      Field(
        name = "dashboardMembers",
        fieldType = ListType(BoardMemberType),
        arguments = BoardIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId <- IO.fromEither(parseBoardId(ctx.arg(BoardIdArg)))
              canRead     <- ctx.ctx.dashboardAccessService.canReadDashboard(dashboardId, currentUserId)
              _           <-
                if (canRead) IO.unit
                else
                  IO.raiseError(BoardAccessDenied("You do not have access to this dashboard"))
              members     <- ctx.ctx.dashboardMembershipService.listMembers(dashboardId)
            } yield members.map(toMemberView)
          }.unsafeToFuture()
      )
    )

  val mutationFields: List[Field[GraphQLContext, Unit]] =
    List(
      createBoardField("createBoard"),
      // TODO: remove this legacy alias after the UI migrates to `createBoard`.
      createBoardField("createDashboard"),
      Field(
        name = "changeDashboardTitle",
        fieldType = BoardType,
        arguments = BoardIdArg :: NameArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId <- IO.fromEither(parseBoardId(ctx.arg(BoardIdArg)))
              changed     <-
                ctx.ctx.dashboardService.changeTitle(
                  dashboardId,
                  currentUserId,
                  BoardName(ctx.arg(NameArg).trim)
                )
              _           <-
                if (changed) IO.unit
                else IO.raiseError(BoardAccessDenied("Board title could not be changed"))
              dashboard   <- ctx.ctx.dashboardService.getDashboard(dashboardId).flatMap(
                               _.liftTo[IO](InvalidUserInput("Board was not found"))
                             )
            } yield toDashboardView(dashboard)
          }.unsafeToFuture()
      ),
      Field(
        name = "changeDashboardDescription",
        fieldType = BoardType,
        arguments = BoardIdArg :: DescriptionArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId <- IO.fromEither(parseBoardId(ctx.arg(BoardIdArg)))
              changed     <-
                ctx.ctx.dashboardService.changeDescription(
                  dashboardId,
                  currentUserId,
                  normalizeDescription(optionalDescriptionArg(ctx))
                )
              _           <-
                if (changed) IO.unit
                else IO.raiseError(BoardAccessDenied("Board description could not be changed"))
              dashboard   <- ctx.ctx.dashboardService.getDashboard(dashboardId).flatMap(
                               _.liftTo[IO](InvalidUserInput("Board was not found"))
                             )
            } yield toDashboardView(dashboard)
          }.unsafeToFuture()
      ),
      Field(
        name = "deactivateDashboard",
        fieldType = BoardType,
        arguments = BoardIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId <- IO.fromEither(parseBoardId(ctx.arg(BoardIdArg)))
              updated     <- ctx.ctx.dashboardService.deactivate(dashboardId, currentUserId)
              _           <-
                if (updated) IO.unit
                else IO.raiseError(BoardAccessDenied("Board could not be deactivated"))
              dashboard   <- ctx.ctx.dashboardService.getDashboard(dashboardId).flatMap(
                               _.liftTo[IO](InvalidUserInput("Board was not found"))
                             )
            } yield toDashboardView(dashboard)
          }.unsafeToFuture()
      ),
      Field(
        name = "inviteDashboardMember",
        fieldType = BoardMemberType,
        arguments = BoardIdArg :: UserIdArg :: RoleIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseBoardId(ctx.arg(BoardIdArg)))
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
                  IO.raiseError(BoardAccessDenied("Member could not be added to the dashboard"))
              member       <-
                ctx.ctx.dashboardMembershipService.findMember(dashboardId, memberUserId).flatMap(
                  _.liftTo[IO](InvalidUserInput("Board member was not found"))
                )
            } yield toMemberView(member)
          }.unsafeToFuture()
      ),
      Field(
        name = "changeDashboardMemberRole",
        fieldType = BoardMemberType,
        arguments = BoardIdArg :: UserIdArg :: RoleIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseBoardId(ctx.arg(BoardIdArg)))
              memberUserId <- IO.fromEither(parseUserId(ctx.arg(UserIdArg)))
              changed      <- ctx.ctx.dashboardService.changeMemberRole(
                                dashboardId,
                                currentUserId,
                                memberUserId,
                                RoleId(ctx.arg(RoleIdArg))
                              )
              _            <-
                if (changed) IO.unit
                else IO.raiseError(BoardAccessDenied("Member role could not be changed"))
              member       <-
                ctx.ctx.dashboardMembershipService.findMember(dashboardId, memberUserId).flatMap(
                  _.liftTo[IO](InvalidUserInput("Board member was not found"))
                )
            } yield toMemberView(member)
          }.unsafeToFuture()
      ),
      Field(
        name = "removeDashboardMember",
        fieldType = BooleanType,
        arguments = BoardIdArg :: UserIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseBoardId(ctx.arg(BoardIdArg)))
              memberUserId <- IO.fromEither(parseUserId(ctx.arg(UserIdArg)))
              removed      <-
                ctx.ctx.dashboardService.removeMember(dashboardId, currentUserId, memberUserId)
            } yield removed
          }.unsafeToFuture()
      )
    )

  private def boardListField(name: String): Field[GraphQLContext, Unit] =
    Field(
      name = name,
      fieldType = ListType(BoardType),
      arguments = KeywordArg :: OwnerUserIdArg :: Nil,
      resolve = ctx =>
        withCurrentUser(ctx) { currentUserId =>
          for {
            filters <- buildBoardQueryFilters(ctx)
            boards  <- ctx.ctx.dashboardService.listDashboardsForUser(currentUserId, filters)
          } yield boards.map(toDashboardView)
        }.unsafeToFuture()
    )

  private def createBoardField(name: String): Field[GraphQLContext, Unit] =
    Field(
      name = name,
      fieldType = BoardType,
      arguments = NameArg :: DescriptionArg :: Nil,
      resolve = ctx =>
        withCurrentUser(ctx) { currentUserId =>
          for {
            now        <- IO.realTimeInstant
            description = optionalDescriptionArg(ctx)
            dashboard   = Board(
                            id = BoardId(UUID.randomUUID()),
                            name = BoardName(ctx.arg(NameArg).trim),
                            description = normalizeDescription(description),
                            active = true,
                            ownerUserId = currentUserId,
                            createdByUserId = currentUserId,
                            createdAt = now,
                            modifiedAt = now,
                            lastModifiedByUserId = currentUserId
                          )
            _          <- ctx.ctx.dashboardService.createDashboard(dashboard)
          } yield toDashboardView(dashboard)
        }.unsafeToFuture()
    )

  private def withCurrentUser[A](
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  )(run: io.github.oleksiybondar.api.domain.user.UserId => IO[A]): IO[A] =
    ctx.ctx.currentUserId match {
      case Some(userId) => run(userId)
      case None         => IO.raiseError(InvalidUserInput("Authentication context is missing"))
    }

  private def parseBoardId(rawId: String): Either[InvalidUserInput, BoardId] =
    Try(UUID.fromString(rawId))
      .toEither
      .left
      .map(_ => InvalidUserInput(s"Invalid UUID: $rawId"))
      .map(BoardId(_))

  private def parseUserId(rawId: String)
      : Either[InvalidUserInput, io.github.oleksiybondar.api.domain.user.UserId] =
    Try(UUID.fromString(rawId))
      .toEither
      .left
      .map(_ => InvalidUserInput(s"Invalid UUID: $rawId"))
      .map(io.github.oleksiybondar.api.domain.user.UserId(_))

  private def normalizeDescription(rawDescription: Option[String]): Option[BoardDescription] =
    rawDescription.map(_.trim).filter(_.nonEmpty).map(BoardDescription(_))

  private def buildBoardQueryFilters(
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  ): IO[BoardQueryFilters] =
    optionalStringArg(ctx, KeywordArg)
      .traverse(_.trim match {
        case ""      => IO.pure(None)
        case keyword => IO.pure(Some(keyword))
      })
      .map(_.flatten)
      .flatMap { keyword =>
        optionalStringArg(ctx, OwnerUserIdArg)
          .traverse(parseUserId(_).liftTo[IO])
          .map(ownerUserId => BoardQueryFilters(keyword = keyword, ownerUserId = ownerUserId))
      }

  private def optionalStringArg(
      ctx: sangria.schema.Context[GraphQLContext, Unit],
      argument: Argument[?]
  ): Option[String] =
    Option(ctx.arg(argument): Any) match {
      case Some(text: String)       => Some(text)
      case Some(Some(text: String)) => Some(text)
      case Some(None)               => None
      case Some(_)                  => None
      case None                     => None
    }

  private def optionalDescriptionArg(
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  ): Option[String] =
    optionalStringArg(ctx, DescriptionArg)

  private def loadUserView(context: GraphQLContext, rawUserId: String): IO[Option[UserView]] =
    parseUserId(rawUserId)
      .liftTo[IO]
      .flatMap(context.userService.getUser)
      .map(_.map(toUserView))

  private def toMemberView(
      member: io.github.oleksiybondar.api.domain.board.BoardMemberWithRole
  ) =
    BoardMemberView(
      boardId = member.member.boardId.value.toString,
      userId = member.member.userId.value.toString,
      createdAt = member.member.createdAt.toString,
      role = BoardRoleView(
        id = member.role.role.id.value.toString,
        name = member.role.role.name.value,
        description = member.role.role.description
      )
    )

  private def toDashboardView(dashboard: io.github.oleksiybondar.api.domain.board.Board) =
    BoardView(
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

  private def toUserView(user: io.github.oleksiybondar.api.domain.user.User): UserView =
    UserView(
      id = user.id.value.toString,
      username = user.username.map(_.value),
      email = user.email.map(_.value),
      firstName = user.firstName.value,
      lastName = user.lastName.value,
      avatarUrl = user.avatarUrl.map(_.value),
      createdAt = user.createdAt.toString
    )
}
