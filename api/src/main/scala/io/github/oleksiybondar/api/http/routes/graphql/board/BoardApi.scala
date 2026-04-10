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
import io.github.oleksiybondar.api.domain.user.{Email, Username}
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import io.github.oleksiybondar.api.http.routes.graphql.permission.RoleApi.PermissionType
import io.github.oleksiybondar.api.http.routes.graphql.ticket.TicketApi
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

  private val BoardIdArg          = Argument("boardId", OptionInputType(StringType))
  private val LegacyBoardIdArg    = Argument("dashboardId", OptionInputType(StringType))
  private val ActiveArg           = Argument("active", OptionInputType(BooleanType))
  private val KeywordArg          = Argument("keyword", OptionInputType(StringType))
  private val NameArg             = Argument("name", StringType)
  private val OwnerUserIdArg      = Argument("ownerUserId", OptionInputType(StringType))
  private val DescriptionArg      = Argument("description", OptionInputType(StringType))
  private val OwnerBoardUserIdArg = Argument("ownerUserId", OptionInputType(StringType))
  private val OwnerLoginArg       = Argument("owner", OptionInputType(StringType))
  private val MemberUserIdArg     = Argument("userId", OptionInputType(StringType))
  private val MemberLoginArg      = Argument("user", OptionInputType(StringType))
  private val UserIdArg           = Argument("userId", StringType)
  private val RoleIdArg           = Argument("roleId", LongType)

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
              if (ctx.value.permissions.nonEmpty) IO.pure(ctx.value.permissions).unsafeToFuture()
              else
                ctx.ctx.roleQueryRepo
                  .findById(RoleId(ctx.value.id.toLong))
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
          resolve =
            ctx =>
              ctx.value.owner match {
                case Some(userView) => IO.pure(Some(userView)).unsafeToFuture()
                case None           => loadUserView(ctx.ctx, ctx.value.ownerUserId).unsafeToFuture()
              }
        ),
        Field(
          "createdBy",
          sangria.schema.OptionType(UserApi.UserType),
          resolve =
            ctx =>
              ctx.value.createdBy match {
                case Some(userView) => IO.pure(Some(userView)).unsafeToFuture()
                case None           => loadUserView(ctx.ctx, ctx.value.createdByUserId).unsafeToFuture()
              }
        ),
        Field(
          "lastModifiedBy",
          sangria.schema.OptionType(UserApi.UserType),
          resolve =
            ctx =>
              ctx.value.lastModifiedBy match {
                case Some(userView) => IO.pure(Some(userView)).unsafeToFuture()
                case None           =>
                  loadUserView(ctx.ctx, ctx.value.lastModifiedByUserId).unsafeToFuture()
              }
        ),
        Field(
          "membersCount",
          IntType,
          resolve =
            ctx =>
              if (ctx.value.membersCount > 0) IO.pure(ctx.value.membersCount).unsafeToFuture()
              else
                ctx.ctx.dashboardMembershipService
                  .listMembers(BoardId(UUID.fromString(ctx.value.id)))
                  .map(_.size)
                  .unsafeToFuture()
        ),
        Field(
          "currentUserRole",
          sangria.schema.OptionType(BoardRoleType),
          resolve =
            ctx =>
              ctx.value.currentUserRole match {
                case Some(roleView) => IO.pure(Some(roleView)).unsafeToFuture()
                case None           =>
                  withCurrentUser(ctx) { currentUserId =>
                    ctx.ctx.dashboardMembershipService
                      .findMember(BoardId(UUID.fromString(ctx.value.id)), currentUserId)
                      .map(
                        _.map(member =>
                          BoardRoleView(
                            id = member.role.role.id.value.toString,
                            name = member.role.role.name.value,
                            description = member.role.role.description
                          )
                        )
                      )
                  }.unsafeToFuture()
              }
        ),
        Field(
          "tickets",
          ListType(TicketApi.TicketType),
          resolve =
            ctx =>
              if (ctx.value.tickets.nonEmpty) IO.pure(ctx.value.tickets).unsafeToFuture()
              else
                TicketApi
                  .ticketsForBoard(ctx.ctx, BoardId(UUID.fromString(ctx.value.id)))
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
        Field("role", BoardRoleType, resolve = _.value.role),
        Field(
          "user",
          sangria.schema.OptionType(UserApi.UserType),
          resolve = _.value.user
        )
      )
    )

  val queryFields: List[Field[GraphQLContext, Unit]] =
    List(
      boardField("board"),
      // TODO: remove this legacy alias after the UI migrates to `board`.
      boardField("dashboard"),
      boardListField("myBoards"),
      // TODO: remove this legacy alias after the UI migrates to `myBoards`.
      boardListField("myDashboards"),
      Field(
        name = "boardMembers",
        fieldType = ListType(BoardMemberType),
        arguments = BoardIdArg :: LegacyBoardIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId <- IO.fromEither(parseRequiredBoardId(ctx))
              canRead     <- ctx.ctx.dashboardAccessService.canReadDashboard(dashboardId, currentUserId)
              _           <-
                if (canRead) IO.unit
                else
                  IO.raiseError(BoardAccessDenied("You do not have access to this dashboard"))
              members     <- ctx.ctx.dashboardMembershipService.listMembers(dashboardId)
              views       <- members.traverse(toMemberView(ctx.ctx, _))
            } yield views
          }.unsafeToFuture()
      ),
      Field(
        name = "dashboardMembers",
        fieldType = ListType(BoardMemberType),
        arguments = BoardIdArg :: LegacyBoardIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId <- IO.fromEither(parseRequiredBoardId(ctx))
              canRead     <- ctx.ctx.dashboardAccessService.canReadDashboard(dashboardId, currentUserId)
              _           <-
                if (canRead) IO.unit
                else
                  IO.raiseError(BoardAccessDenied("You do not have access to this dashboard"))
              members     <- ctx.ctx.dashboardMembershipService.listMembers(dashboardId)
              views       <- members.traverse(toMemberView(ctx.ctx, _))
            } yield views
          }.unsafeToFuture()
      )
    )

  val mutationFields: List[Field[GraphQLContext, Unit]] =
    List(
      createBoardField("createBoard"),
      // TODO: remove this legacy alias after the UI migrates to `createBoard`.
      createBoardField("createDashboard"),
      changeBoardOwnershipField("changeBoardOwnership"),
      Field(
        name = "changeDashboardOwnership",
        fieldType = BoardType,
        arguments = BoardIdArg :: LegacyBoardIdArg :: OwnerBoardUserIdArg :: OwnerLoginArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              boardId     <- IO.fromEither(parseRequiredBoardId(ctx))
              ownerUserId <- resolveOwnerUserId(ctx)
              changed     <-
                ctx.ctx.dashboardService.changeOwnership(boardId, currentUserId, ownerUserId)
              _           <-
                if (changed) IO.unit
                else IO.raiseError(BoardAccessDenied("Board ownership could not be changed"))
              board       <- ctx.ctx.dashboardService.getDashboard(boardId).flatMap(
                               _.liftTo[IO](InvalidUserInput("Board was not found"))
                             )
            } yield toDashboardView(board)
          }.unsafeToFuture()
      ),
      changeBoardTitleField("changeBoardTitle"),
      Field(
        name = "changeDashboardTitle",
        fieldType = BoardType,
        arguments = BoardIdArg :: LegacyBoardIdArg :: NameArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId <- IO.fromEither(parseRequiredBoardId(ctx))
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
      changeBoardDescriptionField("changeBoardDescription"),
      Field(
        name = "changeDashboardDescription",
        fieldType = BoardType,
        arguments = BoardIdArg :: LegacyBoardIdArg :: DescriptionArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId <- IO.fromEither(parseRequiredBoardId(ctx))
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
      deactivateBoardField("deactivateBoard"),
      activateBoardField("activateBoard"),
      Field(
        name = "activateDashboard",
        fieldType = BoardType,
        arguments = BoardIdArg :: LegacyBoardIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              boardId <- IO.fromEither(parseRequiredBoardId(ctx))
              updated <- ctx.ctx.dashboardService.activate(boardId, currentUserId)
              _       <-
                if (updated) IO.unit
                else IO.raiseError(BoardAccessDenied("Board could not be activated"))
              board   <- ctx.ctx.dashboardService.getDashboard(boardId).flatMap(
                           _.liftTo[IO](InvalidUserInput("Board was not found"))
                         )
            } yield toDashboardView(board)
          }.unsafeToFuture()
      ),
      Field(
        name = "deactivateDashboard",
        fieldType = BoardType,
        arguments = BoardIdArg :: LegacyBoardIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId <- IO.fromEither(parseRequiredBoardId(ctx))
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
        name = "inviteBoardMember",
        fieldType = BoardMemberType,
        arguments =
          BoardIdArg :: LegacyBoardIdArg :: MemberUserIdArg :: MemberLoginArg :: RoleIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseRequiredBoardId(ctx))
              memberUserId <- resolveMemberUserId(ctx)
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
              view         <- toMemberView(ctx.ctx, member)
            } yield view
          }.unsafeToFuture()
      ),
      Field(
        name = "inviteDashboardMember",
        fieldType = BoardMemberType,
        arguments =
          BoardIdArg :: LegacyBoardIdArg :: MemberUserIdArg :: MemberLoginArg :: RoleIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseRequiredBoardId(ctx))
              memberUserId <- resolveMemberUserId(ctx)
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
              view         <- toMemberView(ctx.ctx, member)
            } yield view
          }.unsafeToFuture()
      ),
      Field(
        name = "changeBoardMemberRole",
        fieldType = BoardMemberType,
        arguments = BoardIdArg :: LegacyBoardIdArg :: UserIdArg :: RoleIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseRequiredBoardId(ctx))
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
              view         <- toMemberView(ctx.ctx, member)
            } yield view
          }.unsafeToFuture()
      ),
      Field(
        name = "changeDashboardMemberRole",
        fieldType = BoardMemberType,
        arguments = BoardIdArg :: LegacyBoardIdArg :: UserIdArg :: RoleIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseRequiredBoardId(ctx))
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
              view         <- toMemberView(ctx.ctx, member)
            } yield view
          }.unsafeToFuture()
      ),
      Field(
        name = "removeBoardMember",
        fieldType = BooleanType,
        arguments = BoardIdArg :: LegacyBoardIdArg :: UserIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseRequiredBoardId(ctx))
              memberUserId <- IO.fromEither(parseUserId(ctx.arg(UserIdArg)))
              _            <- validateMemberRemoval(ctx.ctx, dashboardId, currentUserId, memberUserId)
              removed      <-
                ctx.ctx.dashboardService.removeMember(dashboardId, currentUserId, memberUserId)
              _            <-
                if (removed) IO.unit
                else IO.raiseError(BoardAccessDenied("Member could not be removed from the board"))
            } yield removed
          }.unsafeToFuture()
      ),
      Field(
        name = "removeDashboardMember",
        fieldType = BooleanType,
        arguments = BoardIdArg :: LegacyBoardIdArg :: UserIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              dashboardId  <- IO.fromEither(parseRequiredBoardId(ctx))
              memberUserId <- IO.fromEither(parseUserId(ctx.arg(UserIdArg)))
              _            <- validateMemberRemoval(ctx.ctx, dashboardId, currentUserId, memberUserId)
              removed      <-
                ctx.ctx.dashboardService.removeMember(dashboardId, currentUserId, memberUserId)
              _            <-
                if (removed) IO.unit
                else IO.raiseError(BoardAccessDenied("Member could not be removed from the board"))
            } yield removed
          }.unsafeToFuture()
      )
    )

  private def boardField(name: String): Field[GraphQLContext, Unit] =
    Field(
      name = name,
      fieldType = sangria.schema.OptionType(BoardType),
      arguments = BoardIdArg :: LegacyBoardIdArg :: Nil,
      resolve = ctx =>
        withCurrentUser(ctx) { currentUserId =>
          for {
            boardId <- IO.fromEither(parseRequiredBoardId(ctx))
            canRead <- ctx.ctx.dashboardAccessService.canReadDashboard(boardId, currentUserId)
            board   <-
              if (canRead) ctx.ctx.boardQueryRepo.findById(boardId, currentUserId)
              else IO.raiseError(BoardAccessDenied("You do not have access to this board"))
          } yield board.map(toDashboardView)
        }.unsafeToFuture()
    )

  private def boardListField(name: String): Field[GraphQLContext, Unit] =
    Field(
      name = name,
      fieldType = ListType(BoardType),
      arguments = ActiveArg :: KeywordArg :: OwnerUserIdArg :: Nil,
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

  private def changeBoardOwnershipField(name: String): Field[GraphQLContext, Unit] =
    Field(
      name = name,
      fieldType = BoardType,
      arguments = BoardIdArg :: LegacyBoardIdArg :: OwnerBoardUserIdArg :: OwnerLoginArg :: Nil,
      resolve = ctx =>
        withCurrentUser(ctx) { currentUserId =>
          for {
            boardId     <- IO.fromEither(parseRequiredBoardId(ctx))
            ownerUserId <- resolveOwnerUserId(ctx)
            changed     <-
              ctx.ctx.dashboardService.changeOwnership(boardId, currentUserId, ownerUserId)
            _           <-
              if (changed) IO.unit
              else IO.raiseError(BoardAccessDenied("Board ownership could not be changed"))
            board       <- ctx.ctx.dashboardService.getDashboard(boardId).flatMap(
                             _.liftTo[IO](InvalidUserInput("Board was not found"))
                           )
          } yield toDashboardView(board)
        }.unsafeToFuture()
    )

  private def deactivateBoardField(name: String): Field[GraphQLContext, Unit] =
    Field(
      name = name,
      fieldType = BoardType,
      arguments = BoardIdArg :: LegacyBoardIdArg :: Nil,
      resolve = ctx =>
        withCurrentUser(ctx) { currentUserId =>
          for {
            boardId <- IO.fromEither(parseRequiredBoardId(ctx))
            updated <- ctx.ctx.dashboardService.deactivate(boardId, currentUserId)
            _       <-
              if (updated) IO.unit
              else IO.raiseError(BoardAccessDenied("Board could not be deactivated"))
            board   <- ctx.ctx.dashboardService.getDashboard(boardId).flatMap(
                         _.liftTo[IO](InvalidUserInput("Board was not found"))
                       )
          } yield toDashboardView(board)
        }.unsafeToFuture()
    )

  private def activateBoardField(name: String): Field[GraphQLContext, Unit] =
    Field(
      name = name,
      fieldType = BoardType,
      arguments = BoardIdArg :: LegacyBoardIdArg :: Nil,
      resolve = ctx =>
        withCurrentUser(ctx) { currentUserId =>
          for {
            boardId <- IO.fromEither(parseRequiredBoardId(ctx))
            updated <- ctx.ctx.dashboardService.activate(boardId, currentUserId)
            _       <-
              if (updated) IO.unit
              else IO.raiseError(BoardAccessDenied("Board could not be activated"))
            board   <- ctx.ctx.dashboardService.getDashboard(boardId).flatMap(
                         _.liftTo[IO](InvalidUserInput("Board was not found"))
                       )
          } yield toDashboardView(board)
        }.unsafeToFuture()
    )

  private def changeBoardTitleField(name: String): Field[GraphQLContext, Unit] =
    Field(
      name = name,
      fieldType = BoardType,
      arguments = BoardIdArg :: LegacyBoardIdArg :: NameArg :: Nil,
      resolve = ctx =>
        withCurrentUser(ctx) { currentUserId =>
          for {
            boardId <- IO.fromEither(parseRequiredBoardId(ctx))
            changed <-
              ctx.ctx.dashboardService.changeTitle(
                boardId,
                currentUserId,
                BoardName(ctx.arg(NameArg).trim)
              )
            _       <-
              if (changed) IO.unit
              else IO.raiseError(BoardAccessDenied("Board title could not be changed"))
            board   <- ctx.ctx.dashboardService.getDashboard(boardId).flatMap(
                         _.liftTo[IO](InvalidUserInput("Board was not found"))
                       )
          } yield toDashboardView(board)
        }.unsafeToFuture()
    )

  private def changeBoardDescriptionField(name: String): Field[GraphQLContext, Unit] =
    Field(
      name = name,
      fieldType = BoardType,
      arguments = BoardIdArg :: LegacyBoardIdArg :: DescriptionArg :: Nil,
      resolve = ctx =>
        withCurrentUser(ctx) { currentUserId =>
          for {
            boardId <- IO.fromEither(parseRequiredBoardId(ctx))
            changed <-
              ctx.ctx.dashboardService.changeDescription(
                boardId,
                currentUserId,
                normalizeDescription(optionalDescriptionArg(ctx))
              )
            _       <-
              if (changed) IO.unit
              else IO.raiseError(BoardAccessDenied("Board description could not be changed"))
            board   <- ctx.ctx.dashboardService.getDashboard(boardId).flatMap(
                         _.liftTo[IO](InvalidUserInput("Board was not found"))
                       )
          } yield toDashboardView(board)
        }.unsafeToFuture()
    )

  private def withCurrentUser[A, B](
      ctx: sangria.schema.Context[GraphQLContext, B]
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

  private def parseRequiredBoardId(
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  ): Either[InvalidUserInput, BoardId] =
    optionalStringArg(ctx, BoardIdArg)
      .orElse(optionalStringArg(ctx, LegacyBoardIdArg))
      .toRight(InvalidUserInput("Board id is required"))
      .flatMap(parseBoardId)

  private def parseUserId(rawId: String)
      : Either[InvalidUserInput, io.github.oleksiybondar.api.domain.user.UserId] =
    Try(UUID.fromString(rawId))
      .toEither
      .left
      .map(_ => InvalidUserInput(s"Invalid UUID: $rawId"))
      .map(io.github.oleksiybondar.api.domain.user.UserId(_))

  private def resolveOwnerUserId(
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  ): IO[io.github.oleksiybondar.api.domain.user.UserId] =
    optionalStringArg(ctx, OwnerLoginArg)
      .map(_.trim)
      .filter(_.nonEmpty) match {
      case Some(ownerLogin) =>
        resolveUserIdByLogin(ctx.ctx, ownerLogin)
      case None             =>
        optionalStringArg(ctx, OwnerBoardUserIdArg)
          .toRight(InvalidUserInput("Owner is required"))
          .flatMap(parseUserId)
          .liftTo[IO]
    }

  private def resolveMemberUserId(
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  ): IO[io.github.oleksiybondar.api.domain.user.UserId] =
    optionalStringArg(ctx, MemberLoginArg)
      .map(_.trim)
      .filter(_.nonEmpty) match {
      case Some(memberLogin) =>
        resolveUserIdByLogin(
          ctx.ctx,
          memberLogin,
          notFoundMessage = "Invited user was not found"
        )
      case None              =>
        optionalStringArg(ctx, MemberUserIdArg)
          .toRight(InvalidUserInput("User is required"))
          .flatMap(parseUserId)
          .liftTo[IO]
    }

  private def resolveUserIdByLogin(
      context: GraphQLContext,
      ownerLogin: String,
      notFoundMessage: String = "Owner user was not found"
  ): IO[io.github.oleksiybondar.api.domain.user.UserId] = {
    val lookup =
      if (ownerLogin.contains("@")) context.userService.getByEmail(Email(ownerLogin))
      else context.userService.getByUsername(Username(ownerLogin))

    lookup.flatMap(
      _.map(_.id).liftTo[IO](InvalidUserInput(notFoundMessage))
    )
  }

  private def normalizeDescription(rawDescription: Option[String]): Option[BoardDescription] =
    rawDescription.map(_.trim).filter(_.nonEmpty).map(BoardDescription(_))

  private def validateMemberRemoval(
      context: GraphQLContext,
      boardId: BoardId,
      currentUserId: io.github.oleksiybondar.api.domain.user.UserId,
      memberUserId: io.github.oleksiybondar.api.domain.user.UserId
  ): IO[Unit] =
    if (memberUserId == currentUserId) {
      IO.raiseError(InvalidUserInput("You cannot remove yourself from the board"))
    } else {
      context.dashboardMembershipService.listMembers(boardId).flatMap { members =>
        if (members.size <= 1) {
          IO.raiseError(InvalidUserInput("The last board member cannot be removed"))
        } else {
          IO.unit
        }
      }
    }

  private def buildBoardQueryFilters(
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  ): IO[BoardQueryFilters] =
    optionalBooleanArg(ctx, ActiveArg).flatMap { active =>
      optionalStringArg(ctx, KeywordArg)
        .traverse(_.trim match {
          case ""      => IO.pure(None)
          case keyword => IO.pure(Some(keyword))
        })
        .map(_.flatten)
        .flatMap { keyword =>
          optionalStringArg(ctx, OwnerUserIdArg)
            .traverse(parseUserId(_).liftTo[IO])
            .map(ownerUserId =>
              BoardQueryFilters(active = active, keyword = keyword, ownerUserId = ownerUserId)
            )
        }
    }

  private def optionalBooleanArg(
      ctx: sangria.schema.Context[GraphQLContext, Unit],
      argument: Argument[?]
  ): IO[Option[Boolean]] =
    IO.pure(
      Option(ctx.arg(argument): Any) match {
        case Some(value: Boolean)       => Some(value)
        case Some(Some(value: Boolean)) => Some(value)
        case Some(None)                 => None
        case Some(_)                    => None
        case None                       => None
      }
    )

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
      context: GraphQLContext,
      member: io.github.oleksiybondar.api.domain.board.BoardMemberWithRole
  ): IO[BoardMemberView] =
    loadUserView(context, member.member.userId.value.toString).map { user =>
      BoardMemberView(
        boardId = member.member.boardId.value.toString,
        userId = member.member.userId.value.toString,
        createdAt = member.member.createdAt.toString,
        role = BoardRoleView(
          id = member.role.role.id.value.toString,
          name = member.role.role.name.value,
          description = member.role.role.description
        ),
        user = user
      )
    }

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

  private def toDashboardView(
      board: io.github.oleksiybondar.api.infrastructure.db.board.BoardQueryRow
  ): BoardView =
    BoardView(
      id = board.id.value.toString,
      name = board.name,
      description = board.description,
      active = board.active,
      ownerUserId = board.ownerUserId,
      createdByUserId = board.createdByUserId,
      createdAt = board.createdAt,
      modifiedAt = board.modifiedAt,
      lastModifiedByUserId = board.lastModifiedByUserId,
      owner = Some(toUserView(board.owner)),
      createdBy = Some(toUserView(board.createdBy)),
      lastModifiedBy = Some(toUserView(board.lastModifiedBy)),
      membersCount = board.membersCount,
      currentUserRole = board.currentUserRole.map(toBoardRoleView),
      tickets = board.tickets.map(toTicketView)
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

  private def toUserView(
      user: io.github.oleksiybondar.api.infrastructure.db.board.BoardQueryUserRow
  ): UserView =
    UserView(
      id = user.id,
      username = None,
      email = None,
      firstName = user.firstName,
      lastName = user.lastName,
      avatarUrl = user.avatarUrl,
      createdAt = ""
    )

  private def toBoardRoleView(
      role: io.github.oleksiybondar.api.infrastructure.db.board.BoardQueryRoleRow
  ): BoardRoleView =
    BoardRoleView(
      id = role.id,
      name = role.name,
      description = role.description,
      permissions = role.permissions
    )

  private def toTicketView(
      ticket: io.github.oleksiybondar.api.infrastructure.db.board.BoardQueryTicketRow
  ): io.github.oleksiybondar.api.http.routes.graphql.ticket.TicketView =
    io.github.oleksiybondar.api.http.routes.graphql.ticket.TicketView(
      id = ticket.id,
      boardId = ticket.boardId,
      name = ticket.name,
      description = ticket.description,
      acceptanceCriteria = ticket.acceptanceCriteria,
      estimatedMinutes = ticket.estimatedMinutes,
      createdByUserId = ticket.createdByUserId,
      assignedToUserId = ticket.assignedToUserId,
      lastModifiedByUserId = ticket.lastModifiedByUserId,
      createdAt = ticket.createdAt,
      modifiedAt = ticket.modifiedAt,
      stateId = ticket.stateId.value,
      trackedMinutes = ticket.trackedMinutes
    )
}
