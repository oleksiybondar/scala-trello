package io.github.oleksiybondar.api.http.routes.graphql.ticket

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.BoardId
import io.github.oleksiybondar.api.domain.ticket.{
  CreateTicketCommand,
  Ticket,
  TicketAcceptanceCriteria,
  TicketDescription,
  TicketId,
  TicketName,
  TicketStateId
}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import io.github.oleksiybondar.api.http.routes.graphql.comment.{
  CommentApi,
  CommentTicketSummaryView,
  CommentView
}
import io.github.oleksiybondar.api.http.routes.graphql.timeTracking.{
  TimeTrackingApi,
  TimeTrackingEntryView,
  TimeTrackingTicketSummaryView
}
import io.github.oleksiybondar.api.http.routes.graphql.user.{UserApi, UserView}
import sangria.execution.UserFacingError
import sangria.schema.{
  Argument,
  Field,
  IntType,
  ListType,
  ObjectType,
  OptionInputType,
  OptionType,
  StringType,
  fields
}

import java.util.UUID
import scala.util.Try

object TicketApi {

  final case class InvalidTicketInput(override val getMessage: String)
      extends IllegalArgumentException(getMessage)
      with UserFacingError

  final case class TicketAccessDenied(override val getMessage: String)
      extends IllegalArgumentException(getMessage)
      with UserFacingError

  private val TicketIdArg       = Argument("ticketId", StringType)
  private val BoardIdArg        = Argument("boardId", OptionInputType(StringType))
  private val LegacyBoardIdArg  = Argument("dashboardId", OptionInputType(StringType))
  private val TitleArg          = Argument("title", StringType)
  private val DescriptionArg    = Argument("description", OptionInputType(StringType))
  private val AcceptanceArg     = Argument("acceptanceCriteria", OptionInputType(StringType))
  private val EstimatedArg      = Argument("estimatedMinutes", OptionInputType(IntType))
  private val AssignedUserIdArg = Argument("assignedToUserId", OptionInputType(StringType))

  val TicketBoardSummaryType: ObjectType[GraphQLContext, TicketBoardSummaryView] =
    ObjectType(
      name = "TicketBoardSummaryView",
      fields[GraphQLContext, TicketBoardSummaryView](
        Field("id", StringType, resolve = _.value.id),
        Field("name", StringType, resolve = _.value.name),
        Field("active", sangria.schema.BooleanType, resolve = _.value.active)
      )
    )

  val TicketType: ObjectType[GraphQLContext, TicketView] =
    ObjectType(
      name = "TicketView",
      fields[GraphQLContext, TicketView](
        Field("id", StringType, resolve = _.value.id),
        Field("boardId", StringType, resolve = _.value.boardId),
        Field("name", StringType, resolve = _.value.name),
        Field("description", OptionType(StringType), resolve = _.value.description),
        Field(
          "acceptanceCriteria",
          OptionType(StringType),
          resolve = _.value.acceptanceCriteria
        ),
        Field("estimatedMinutes", OptionType(IntType), resolve = _.value.estimatedMinutes),
        Field("commentsCount", IntType, resolve = _.value.commentsCount),
        Field("trackedMinutes", IntType, resolve = _.value.trackedMinutes),
        Field(
          "status",
          OptionType(StringType),
          resolve =
            ctx =>
              ctx.ctx.ticketStateRepo
                .findById(TicketStateId(ctx.value.stateId))
                .map(_.map(_.name.value))
                .unsafeToFuture()
        ),
        Field("createdByUserId", StringType, resolve = _.value.createdByUserId),
        Field(
          "assignedToUserId",
          OptionType(StringType),
          resolve = _.value.assignedToUserId
        ),
        Field("lastModifiedByUserId", StringType, resolve = _.value.lastModifiedByUserId),
        Field("createdAt", StringType, resolve = _.value.createdAt),
        Field("modifiedAt", StringType, resolve = _.value.modifiedAt),
        Field(
          "board",
          OptionType(TicketBoardSummaryType),
          resolve =
            ctx =>
              ctx.value.board match {
                case Some(boardView) => IO.pure(Some(boardView)).unsafeToFuture()
                case None            => IO.pure(None).unsafeToFuture()
              }
        ),
        Field(
          "createdBy",
          OptionType(UserApi.UserType),
          resolve =
            ctx =>
              ctx.value.createdBy match {
                case Some(userView) => IO.pure(Some(userView)).unsafeToFuture()
                case None           => loadUserView(ctx.ctx, ctx.value.createdByUserId).unsafeToFuture()
              }
        ),
        Field(
          "assignedTo",
          OptionType(UserApi.UserType),
          resolve =
            ctx =>
              ctx.value.assignedTo match {
                case Some(userView) => IO.pure(Some(userView)).unsafeToFuture()
                case None           =>
                  ctx.value.assignedToUserId match {
                    case Some(userId) => loadUserView(ctx.ctx, userId).unsafeToFuture()
                    case None         => IO.pure(None).unsafeToFuture()
                  }
              }
        ),
        Field(
          "lastModifiedBy",
          OptionType(UserApi.UserType),
          resolve =
            ctx =>
              ctx.value.lastModifiedBy match {
                case Some(userView) => IO.pure(Some(userView)).unsafeToFuture()
                case None           =>
                  loadUserView(ctx.ctx, ctx.value.lastModifiedByUserId).unsafeToFuture()
              }
        ),
        Field(
          "comments",
          ListType(CommentApi.CommentType),
          resolve =
            ctx =>
              if (ctx.value.comments.nonEmpty) IO.pure(ctx.value.comments).unsafeToFuture()
              else
                ctx.ctx.commentQueryRepo
                  .listByTicket(TicketId(ctx.value.id.toLong))
                  .map(_.map(toCommentView))
                  .unsafeToFuture()
        ),
        Field(
          "timeEntries",
          ListType(TimeTrackingApi.TimeTrackingEntryType),
          resolve =
            ctx =>
              if (ctx.value.timeEntries.nonEmpty) IO.pure(ctx.value.timeEntries).unsafeToFuture()
              else
                ctx.ctx.timeTrackingQueryRepo
                  .listByTicket(TicketId(ctx.value.id.toLong))
                  .map(_.map(toTimeTrackingView))
                  .unsafeToFuture()
        )
      )
    )

  val queryFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "ticket",
        fieldType = OptionType(TicketType),
        arguments = TicketIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              ticketId <- IO.fromEither(parseTicketId(ctx.arg(TicketIdArg)))
              ticket   <- ctx.ctx.ticketQueryRepo.findById(ticketId)
              result   <- ticket match {
                            case None        => IO.pure(None)
                            case Some(value) =>
                              ctx.ctx.dashboardAccessService
                                .canReadTicket(
                                  BoardId(UUID.fromString(value.board.id)),
                                  currentUserId
                                )
                                .flatMap {
                                  case true  => IO.pure(Some(toView(value)))
                                  case false => IO.pure(None)
                                }
                          }
            } yield result
          }.unsafeToFuture()
      ),
      Field(
        name = "tickets",
        fieldType = ListType(TicketType),
        arguments = BoardIdArg :: LegacyBoardIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              boardId <- IO.fromEither(parseRequiredBoardId(ctx))
              tickets <- ctx.ctx.ticketService.listTickets(boardId, currentUserId)
            } yield tickets.map(toView)
          }.unsafeToFuture()
      )
    )

  val mutationFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "createTicket",
        fieldType = TicketType,
        arguments =
          BoardIdArg :: LegacyBoardIdArg :: TitleArg :: DescriptionArg :: AcceptanceArg :: EstimatedArg :: AssignedUserIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              boardId       <- IO.fromEither(parseRequiredBoardId(ctx))
              title         <- IO.fromEither(parseRequiredTitle(ctx.arg(TitleArg)))
              estimated     <- IO.fromEither(parseEstimatedMinutes(ctx.arg(EstimatedArg)))
              assignedToId  <- IO.fromEither(parseOptionalUserId(ctx.arg(AssignedUserIdArg)))
              createdTicket <- ctx.ctx.ticketService.createTicket(
                                 CreateTicketCommand(
                                   boardId = boardId,
                                   name = title,
                                   description = normalizeOptionalText(ctx.arg(DescriptionArg))
                                     .map(TicketDescription(_)),
                                   acceptanceCriteria =
                                     normalizeOptionalText(ctx.arg(AcceptanceArg))
                                       .map(TicketAcceptanceCriteria(_)),
                                   assignedToUserId = assignedToId,
                                   originalEstimatedMinutes = estimated,
                                   stateId = TicketStateId(1)
                                 ),
                                 currentUserId
                               )
              ticket        <-
                createdTicket.liftTo[IO](
                  TicketAccessDenied("Ticket could not be created")
                )
            } yield toView(ticket)
          }.unsafeToFuture()
      ),
      Field(
        name = "changeTicketTitle",
        fieldType = TicketType,
        arguments = TicketIdArg :: TitleArg :: Nil,
        resolve = ctx =>
          updateAndLoadTicket(ctx) { currentUserId =>
            for {
              ticketId <- IO.fromEither(parseTicketId(ctx.arg(TicketIdArg)))
              title    <- IO.fromEither(parseRequiredTitle(ctx.arg(TitleArg)))
              updated  <- ctx.ctx.ticketService.changeTitle(ticketId, currentUserId, title)
            } yield (ticketId, updated, "Ticket title could not be changed")
          }.unsafeToFuture()
      ),
      Field(
        name = "changeTicketDescription",
        fieldType = TicketType,
        arguments = TicketIdArg :: DescriptionArg :: Nil,
        resolve = ctx =>
          updateAndLoadTicket(ctx) { currentUserId =>
            for {
              ticketId <- IO.fromEither(parseTicketId(ctx.arg(TicketIdArg)))
              updated  <- ctx.ctx.ticketService.changeDescription(
                            ticketId,
                            currentUserId,
                            normalizeOptionalText(ctx.arg(DescriptionArg)).map(TicketDescription(_))
                          )
            } yield (ticketId, updated, "Ticket description could not be changed")
          }.unsafeToFuture()
      ),
      Field(
        name = "changeTicketAcceptanceCriteria",
        fieldType = TicketType,
        arguments = TicketIdArg :: AcceptanceArg :: Nil,
        resolve = ctx =>
          updateAndLoadTicket(ctx) { currentUserId =>
            for {
              ticketId <- IO.fromEither(parseTicketId(ctx.arg(TicketIdArg)))
              updated  <- ctx.ctx.ticketService.changeAcceptanceCriteria(
                            ticketId,
                            currentUserId,
                            normalizeOptionalText(ctx.arg(AcceptanceArg))
                              .map(TicketAcceptanceCriteria(_))
                          )
            } yield (ticketId, updated, "Ticket acceptance criteria could not be changed")
          }.unsafeToFuture()
      ),
      Field(
        name = "changeTicketEstimatedTime",
        fieldType = TicketType,
        arguments = TicketIdArg :: EstimatedArg :: Nil,
        resolve = ctx =>
          updateAndLoadTicket(ctx) { currentUserId =>
            for {
              ticketId  <- IO.fromEither(parseTicketId(ctx.arg(TicketIdArg)))
              estimated <- IO.fromEither(parseEstimatedMinutes(ctx.arg(EstimatedArg)))
              updated   <-
                ctx.ctx.ticketService.changeEstimatedTime(ticketId, currentUserId, estimated)
            } yield (ticketId, updated, "Ticket estimated time could not be changed")
          }.unsafeToFuture()
      ),
      Field(
        name = "reassignTicket",
        fieldType = TicketType,
        arguments = TicketIdArg :: AssignedUserIdArg :: Nil,
        resolve = ctx =>
          updateAndLoadTicket(ctx) { currentUserId =>
            for {
              ticketId     <- IO.fromEither(parseTicketId(ctx.arg(TicketIdArg)))
              assignedToId <- IO.fromEither(parseOptionalUserId(ctx.arg(AssignedUserIdArg)))
              updated      <-
                ctx.ctx.ticketService.reassignTicket(ticketId, currentUserId, assignedToId)
            } yield (ticketId, updated, "Ticket could not be reassigned")
          }.unsafeToFuture()
      )
    )

  def ticketsForBoard(
      graphQLContext: GraphQLContext,
      boardId: BoardId
  ): IO[List[TicketView]] =
    graphQLContext.currentUserId match {
      case Some(currentUserId) =>
        graphQLContext.ticketService.listTickets(boardId, currentUserId).map(_.map(toView))
      case None                =>
        IO.raiseError(InvalidTicketInput("Authentication context is missing"))
    }

  private def updateAndLoadTicket(
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  )(
      action: UserId => IO[(TicketId, Boolean, String)]
  ): IO[TicketView] =
    withCurrentUser(ctx) { currentUserId =>
      for {
        result                      <- action(currentUserId)
        (ticketId, updated, message) = result
        _                           <-
          if (updated) IO.unit
          else IO.raiseError(TicketAccessDenied(message))
        ticket                      <- ctx.ctx.ticketService.getTicket(ticketId).flatMap(
                                         _.liftTo[IO](InvalidTicketInput("Ticket was not found"))
                                       )
      } yield toView(ticket)
    }

  private def loadUserView(ctx: GraphQLContext, rawUserId: String): IO[Option[UserView]] =
    parseUserId(rawUserId) match {
      case Left(_)       => IO.pure(None)
      case Right(userId) => ctx.userService.getUser(userId).map(_.map(toUserView))
    }

  private def toCommentView(
      row: io.github.oleksiybondar.api.infrastructure.db.comment.CommentQueryRow
  ): CommentView =
    CommentView(
      id = row.id.value.toString,
      ticketId = row.ticketId.value.toString,
      authorUserId = row.authorUserId,
      createdAt = row.createdAt,
      modifiedAt = row.modifiedAt,
      message = row.message,
      relatedCommentId = row.relatedCommentId,
      user = Some(
        UserView(
          id = row.authorUserId,
          username = row.authorUsername,
          email = row.authorEmail,
          firstName = row.authorFirstName,
          lastName = row.authorLastName,
          avatarUrl = row.authorAvatarUrl,
          createdAt = row.authorCreatedAt
        )
      ),
      ticket = Some(
        CommentTicketSummaryView(
          id = row.ticketId.value.toString,
          boardId = row.ticketBoardId,
          title = row.ticketTitle
        )
      )
    )

  private def toTimeTrackingView(
      row: io.github.oleksiybondar.api.infrastructure.db.timeTracking.TimeTrackingQueryRow
  ): TimeTrackingEntryView =
    TimeTrackingEntryView(
      id = row.id.value.toString,
      ticketId = row.ticketId.value.toString,
      userId = row.userId,
      activityId = row.activityId.value.toString,
      activityCode = row.activityCode,
      activityName = row.activityName,
      durationMinutes = row.durationMinutes,
      loggedAt = row.loggedAt,
      description = row.description,
      user = Some(
        UserView(
          id = row.userId,
          username = row.username,
          email = row.email,
          firstName = row.firstName,
          lastName = row.lastName,
          avatarUrl = row.avatarUrl,
          createdAt = row.userCreatedAt
        )
      ),
      ticket = Some(
        TimeTrackingTicketSummaryView(
          id = row.ticketId.value.toString,
          title = row.ticketTitle,
          description = row.ticketDescription
        )
      )
    )

  private def parseTicketId(rawId: String): Either[InvalidTicketInput, TicketId] =
    Try(rawId.toLong)
      .toEither
      .left
      .map(_ => InvalidTicketInput(s"Invalid ticket id: $rawId"))
      .map(TicketId(_))

  private def parseUserId(rawId: String): Either[InvalidTicketInput, UserId] =
    Try(UUID.fromString(rawId))
      .toEither
      .left
      .map(_ => InvalidTicketInput(s"Invalid UUID: $rawId"))
      .map(UserId(_))

  private def parseOptionalUserId(
      rawId: Option[String]
  ): Either[InvalidTicketInput, Option[UserId]] =
    rawId.map(_.trim).filter(_.nonEmpty).traverse(parseUserId)

  private def parseRequiredBoardId(
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  ): Either[InvalidTicketInput, BoardId] = {
    val rawBoardId: Option[String] = ctx.arg(BoardIdArg).orElse(ctx.arg(LegacyBoardIdArg))

    rawBoardId
  }
    .map(_.trim)
    .filter(_.nonEmpty)
    .toRight(InvalidTicketInput("Board id is required"))
    .flatMap(raw =>
      Try(UUID.fromString(raw))
        .toEither
        .left
        .map(_ => InvalidTicketInput(s"Invalid UUID: $raw"))
        .map(BoardId(_))
    )

  private def parseRequiredTitle(rawTitle: String): Either[InvalidTicketInput, TicketName] =
    Option(rawTitle).map(_.trim).filter(_.nonEmpty) match {
      case Some(value) => Right(TicketName(value))
      case None        => Left(InvalidTicketInput("Title is required"))
    }

  private def parseEstimatedMinutes(
      rawValue: Option[Int]
  ): Either[InvalidTicketInput, Option[Int]] =
    rawValue match {
      case Some(value) if value < 0 =>
        Left(InvalidTicketInput("Estimated minutes must be non-negative"))
      case _                        => Right(rawValue)
    }

  private def normalizeOptionalText(rawValue: Option[String]): Option[String] =
    rawValue.map(_.trim).filter(_.nonEmpty)

  private def withCurrentUser[A](
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  )(run: UserId => IO[A]): IO[A] =
    ctx.ctx.currentUserId match {
      case Some(userId) => run(userId)
      case None         => IO.raiseError(InvalidTicketInput("Authentication context is missing"))
    }

  private def toView(ticket: Ticket): TicketView =
    TicketView(
      id = ticket.id.value.toString,
      boardId = ticket.boardId.value.toString,
      name = ticket.name.value,
      description = ticket.description.map(_.value),
      acceptanceCriteria = ticket.acceptanceCriteria.map(_.value),
      estimatedMinutes = ticket.originalEstimatedMinutes,
      createdByUserId = ticket.createdByUserId.value.toString,
      assignedToUserId = ticket.assignedToUserId.map(_.value.toString),
      lastModifiedByUserId = ticket.lastModifiedByUserId.value.toString,
      createdAt = ticket.createdAt.toString,
      modifiedAt = ticket.modifiedAt.toString,
      stateId = ticket.stateId.value,
      trackedMinutes = 0
    )

  private def toView(
      ticket: io.github.oleksiybondar.api.infrastructure.db.ticket.TicketQueryRow
  ): TicketView =
    TicketView(
      id = ticket.id.value.toString,
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
      commentsCount = ticket.commentsCount,
      trackedMinutes = 0,
      board = Some(
        TicketBoardSummaryView(
          id = ticket.board.id,
          name = ticket.board.name,
          active = ticket.board.active
        )
      ),
      createdBy = Some(toUserView(ticket.createdBy)),
      assignedTo = ticket.assignedTo.map(toUserView),
      lastModifiedBy = Some(toUserView(ticket.lastModifiedBy)),
      timeEntries = ticket.timeEntries.map(toTimeTrackingView)
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
      user: io.github.oleksiybondar.api.infrastructure.db.ticket.TicketQueryUserRow
  ): UserView =
    UserView(
      id = user.id,
      username = user.username,
      email = user.email,
      firstName = user.firstName,
      lastName = user.lastName,
      avatarUrl = user.avatarUrl,
      createdAt = user.createdAt
    )

  private def toTimeTrackingView(
      row: io.github.oleksiybondar.api.infrastructure.db.ticket.TicketQueryTimeEntryRow
  ): TimeTrackingEntryView =
    TimeTrackingEntryView(
      id = row.id.value.toString,
      ticketId = row.ticketId.value.toString,
      userId = row.userId,
      activityId = row.activityId.value.toString,
      activityCode = Some(row.activityCode),
      activityName = Some(row.activityName),
      durationMinutes = row.durationMinutes,
      loggedAt = row.loggedAt,
      description = row.description,
      user = Some(toUserView(row.user)),
      ticket = None
    )
}
