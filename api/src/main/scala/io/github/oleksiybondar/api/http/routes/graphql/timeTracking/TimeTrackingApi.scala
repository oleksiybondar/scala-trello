package io.github.oleksiybondar.api.http.routes.graphql.timeTracking

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.timeTracking.{
  CreateTimeTrackingEntryCommand,
  TimeTrackingActivityId,
  TimeTrackingDurationMinutes,
  TimeTrackingEntry,
  TimeTrackingEntryDescription,
  TimeTrackingEntryId,
  UpdateTimeTrackingEntryCommand
}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
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
  OptionType,
  StringType,
  fields
}

import java.time.Instant
import java.util.UUID
import scala.util.Try

object TimeTrackingApi {

  final case class InvalidTimeTrackingInput(override val getMessage: String)
      extends IllegalArgumentException(getMessage)
      with UserFacingError

  final case class TimeTrackingAccessDenied(override val getMessage: String)
      extends IllegalArgumentException(getMessage)
      with UserFacingError

  private val EntryIdArg     = Argument("entryId", StringType)
  private val TicketIdArg    = Argument("ticketId", StringType)
  private val UserIdArg      = Argument("userId", StringType)
  private val ActivityIdArg  = Argument("activityId", LongType)
  private val DurationArg    = Argument("durationMinutes", IntType)
  private val LoggedAtArg    = Argument("loggedAt", StringType)
  private val DescriptionArg = Argument("description", OptionInputType(StringType))

  val TicketSummaryType: ObjectType[GraphQLContext, TimeTrackingTicketSummaryView] =
    ObjectType(
      name = "TimeTrackingTicketSummaryView",
      fields[GraphQLContext, TimeTrackingTicketSummaryView](
        Field("id", StringType, resolve = _.value.id),
        Field("title", StringType, resolve = _.value.title),
        Field("description", OptionType(StringType), resolve = _.value.description)
      )
    )

  val TimeTrackingEntryType: ObjectType[GraphQLContext, TimeTrackingEntryView] =
    ObjectType(
      name = "TimeTrackingEntryView",
      fields[GraphQLContext, TimeTrackingEntryView](
        Field("id", StringType, resolve = _.value.id),
        Field("ticketId", StringType, resolve = _.value.ticketId),
        Field("userId", StringType, resolve = _.value.userId),
        Field("activityId", StringType, resolve = _.value.activityId),
        Field("activityCode", OptionType(StringType), resolve = _.value.activityCode),
        Field("activityName", OptionType(StringType), resolve = _.value.activityName),
        Field("durationMinutes", IntType, resolve = _.value.durationMinutes),
        Field("loggedAt", StringType, resolve = _.value.loggedAt),
        Field("description", OptionType(StringType), resolve = _.value.description),
        Field(
          "ticket",
          OptionType(TicketSummaryType),
          resolve =
            ctx =>
              ctx.value.ticket match {
                case Some(ticketView) => IO.pure(Some(ticketView)).unsafeToFuture()
                case None             =>
                  parseTicketId(ctx.value.ticketId) match {
                    case Left(_)         => IO.pure(None).unsafeToFuture()
                    case Right(ticketId) =>
                      ctx.ctx.ticketService.getTicket(
                        ticketId
                      ).map(_.map(toTicketSummaryView)).unsafeToFuture()
                  }
              }
        ),
        Field(
          "user",
          OptionType(UserApi.UserType),
          resolve =
            ctx =>
              ctx.value.user match {
                case Some(userView) => IO.pure(Some(userView)).unsafeToFuture()
                case None           => UserApi.loadUserView(ctx.ctx, ctx.value.userId).unsafeToFuture()
              }
        )
      )
    )

  val queryFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "timeTrackingEntry",
        fieldType = OptionType(TimeTrackingEntryType),
        arguments = EntryIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              entryId <- IO.fromEither(parseEntryId(ctx.arg(EntryIdArg)))
              entry   <- ctx.ctx.timeTrackingService.getEntry(entryId, currentUserId)
            } yield entry.map(toView)
          }.unsafeToFuture()
      ),
      Field(
        name = "timeTrackingEntriesByUser",
        fieldType = ListType(TimeTrackingEntryType),
        arguments = UserIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              userId  <- IO.fromEither(parseUserId(ctx.arg(UserIdArg)))
              entries <- ctx.ctx.timeTrackingService.listEntriesByUser(userId, currentUserId)
            } yield entries.map(toView)
          }.unsafeToFuture()
      ),
      Field(
        name = "timeTrackingEntriesByTicket",
        fieldType = ListType(TimeTrackingEntryType),
        arguments = TicketIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              ticketId <- IO.fromEither(parseTicketId(ctx.arg(TicketIdArg)))
              entries  <- ctx.ctx.timeTrackingService.listEntriesByTicket(ticketId, currentUserId)
            } yield entries.map(toView)
          }.unsafeToFuture()
      )
    )

  val mutationFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "createTimeTrackingEntry",
        fieldType = TimeTrackingEntryType,
        arguments =
          TicketIdArg :: ActivityIdArg :: DurationArg :: LoggedAtArg :: DescriptionArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              ticketId   <- IO.fromEither(parseTicketId(ctx.arg(TicketIdArg)))
              activityId <- IO.fromEither(parseActivityId(ctx.arg(ActivityIdArg)))
              duration   <- IO.fromEither(parseDuration(ctx.arg(DurationArg)))
              loggedAt   <- IO.fromEither(parseInstant(ctx.arg(LoggedAtArg)))
              created    <- ctx.ctx.timeTrackingService.createEntry(
                              CreateTimeTrackingEntryCommand(
                                ticketId = ticketId,
                                activityId = activityId,
                                durationMinutes = duration,
                                loggedAt = loggedAt,
                                description = normalizeOptionalText(ctx.arg(DescriptionArg))
                                  .map(TimeTrackingEntryDescription(_))
                              ),
                              currentUserId
                            )
              entry      <- created.liftTo[IO](
                              TimeTrackingAccessDenied("Time tracking entry could not be created")
                            )
            } yield toView(entry)
          }.unsafeToFuture()
      ),
      Field(
        name = "updateTimeTrackingActivity",
        fieldType = TimeTrackingEntryType,
        arguments = EntryIdArg :: ActivityIdArg :: Nil,
        resolve = ctx =>
          updateAndLoadEntry(ctx) { currentUserId =>
            for {
              entryId  <- IO.fromEither(parseEntryId(ctx.arg(EntryIdArg)))
              activity <- IO.fromEither(parseActivityId(ctx.arg(ActivityIdArg)))
              existing <- ctx.ctx.timeTrackingService.getOwnEntry(entryId, currentUserId).flatMap(
                            _.liftTo[IO](
                              TimeTrackingAccessDenied("Time tracking entry could not be updated")
                            )
                          )
              updated  <- ctx.ctx.timeTrackingService.updateOwnEntry(
                            entryId,
                            UpdateTimeTrackingEntryCommand(
                              activityId = activity,
                              durationMinutes = existing.durationMinutes,
                              loggedAt = existing.loggedAt,
                              description = existing.description
                            ),
                            currentUserId
                          )
            } yield (entryId, updated, "Time tracking entry could not be updated")
          }.unsafeToFuture()
      ),
      Field(
        name = "updateTimeTrackingDescription",
        fieldType = TimeTrackingEntryType,
        arguments = EntryIdArg :: DescriptionArg :: Nil,
        resolve = ctx =>
          updateAndLoadEntry(ctx) { currentUserId =>
            for {
              entryId    <- IO.fromEither(parseEntryId(ctx.arg(EntryIdArg)))
              description =
                normalizeOptionalText(ctx.arg(DescriptionArg)).map(TimeTrackingEntryDescription(_))
              existing   <- ctx.ctx.timeTrackingService.getOwnEntry(entryId, currentUserId).flatMap(
                              _.liftTo[IO](
                                TimeTrackingAccessDenied("Time tracking entry could not be updated")
                              )
                            )
              updated    <- ctx.ctx.timeTrackingService.updateOwnEntry(
                              entryId,
                              UpdateTimeTrackingEntryCommand(
                                activityId = existing.activityId,
                                durationMinutes = existing.durationMinutes,
                                loggedAt = existing.loggedAt,
                                description = description
                              ),
                              currentUserId
                            )
            } yield (entryId, updated, "Time tracking entry could not be updated")
          }.unsafeToFuture()
      ),
      Field(
        name = "updateTimeTrackingTime",
        fieldType = TimeTrackingEntryType,
        arguments = EntryIdArg :: DurationArg :: LoggedAtArg :: Nil,
        resolve = ctx =>
          updateAndLoadEntry(ctx) { currentUserId =>
            for {
              entryId  <- IO.fromEither(parseEntryId(ctx.arg(EntryIdArg)))
              duration <- IO.fromEither(parseDuration(ctx.arg(DurationArg)))
              loggedAt <- IO.fromEither(parseInstant(ctx.arg(LoggedAtArg)))
              existing <- ctx.ctx.timeTrackingService.getOwnEntry(entryId, currentUserId).flatMap(
                            _.liftTo[IO](
                              TimeTrackingAccessDenied("Time tracking entry could not be updated")
                            )
                          )
              updated  <- ctx.ctx.timeTrackingService.updateOwnEntry(
                            entryId,
                            UpdateTimeTrackingEntryCommand(
                              activityId = existing.activityId,
                              durationMinutes = duration,
                              loggedAt = loggedAt,
                              description = existing.description
                            ),
                            currentUserId
                          )
            } yield (entryId, updated, "Time tracking entry could not be updated")
          }.unsafeToFuture()
      ),
      Field(
        name = "deleteTimeTrackingEntry",
        fieldType = BooleanType,
        arguments = EntryIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              entryId <- IO.fromEither(parseEntryId(ctx.arg(EntryIdArg)))
              deleted <- ctx.ctx.timeTrackingService.deleteOwnEntry(entryId, currentUserId)
              _       <-
                if (deleted) IO.unit
                else IO.raiseError(
                  TimeTrackingAccessDenied("Time tracking entry could not be deleted")
                )
            } yield true
          }.unsafeToFuture()
      )
    )

  private def updateAndLoadEntry(
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  )(
      action: UserId => IO[(TimeTrackingEntryId, Boolean, String)]
  ): IO[TimeTrackingEntryView] =
    withCurrentUser(ctx) { currentUserId =>
      for {
        result                   <- action(currentUserId)
        (entryId, updated, error) = result
        _                        <-
          if (updated) IO.unit else IO.raiseError(TimeTrackingAccessDenied(error))
        entry                    <- ctx.ctx.timeTrackingService.getOwnEntry(entryId, currentUserId).flatMap(
                                      _.liftTo[IO](InvalidTimeTrackingInput("Time tracking entry was not found"))
                                    )
      } yield toView(entry)
    }

  private def withCurrentUser[A](
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  )(action: UserId => IO[A]): IO[A] =
    ctx.ctx.currentUserId match {
      case Some(currentUserId) => action(currentUserId)
      case None                => IO.raiseError(InvalidTimeTrackingInput("Authentication is required"))
    }

  private def parseEntryId(rawId: String): Either[InvalidTimeTrackingInput, TimeTrackingEntryId] =
    Try(rawId.toLong)
      .toEither
      .left
      .map(_ => InvalidTimeTrackingInput(s"Invalid entry id: $rawId"))
      .map(TimeTrackingEntryId(_))

  private def parseTicketId(rawId: String): Either[InvalidTimeTrackingInput, TicketId] =
    Try(rawId.toLong)
      .toEither
      .left
      .map(_ => InvalidTimeTrackingInput(s"Invalid ticket id: $rawId"))
      .map(TicketId(_))

  private def parseUserId(rawId: String): Either[InvalidTimeTrackingInput, UserId] =
    Try(UUID.fromString(rawId))
      .toEither
      .left
      .map(_ => InvalidTimeTrackingInput(s"Invalid UUID: $rawId"))
      .map(UserId(_))

  private def parseActivityId(
      rawId: Long
  ): Either[InvalidTimeTrackingInput, TimeTrackingActivityId] =
    Either.cond(
      rawId > 0,
      TimeTrackingActivityId(rawId),
      InvalidTimeTrackingInput("Activity id must be positive")
    )

  private def parseDuration(
      rawDuration: Int
  ): Either[InvalidTimeTrackingInput, TimeTrackingDurationMinutes] =
    Either.cond(
      rawDuration > 0,
      TimeTrackingDurationMinutes(rawDuration),
      InvalidTimeTrackingInput("Duration minutes must be greater than zero")
    )

  private def parseInstant(rawValue: String): Either[InvalidTimeTrackingInput, Instant] =
    Try(Instant.parse(rawValue.trim))
      .toEither
      .left
      .map(_ => InvalidTimeTrackingInput(s"Invalid instant: $rawValue"))

  private def normalizeOptionalText(rawValue: Option[String]): Option[String] =
    rawValue.map(_.trim).filter(_.nonEmpty)

  private def toView(entry: TimeTrackingEntry): TimeTrackingEntryView =
    TimeTrackingEntryView(
      id = entry.id.value.toString,
      ticketId = entry.ticketId.value.toString,
      userId = entry.userId.value.toString,
      activityId = entry.activityId.value.toString,
      activityCode = None,
      activityName = None,
      durationMinutes = entry.durationMinutes.value,
      loggedAt = entry.loggedAt.toString,
      description = entry.description.map(_.value)
    )

  private def toTicketSummaryView(
      ticket: io.github.oleksiybondar.api.domain.ticket.Ticket
  ): TimeTrackingTicketSummaryView =
    TimeTrackingTicketSummaryView(
      id = ticket.id.value.toString,
      title = ticket.name.value,
      description = ticket.description.map(_.value)
    )

}
