package io.github.oleksiybondar.api.http.routes.graphql.comment

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.comment.{
  Comment,
  CommentId,
  CommentMessage,
  CreateCommentCommand
}
import io.github.oleksiybondar.api.domain.ticket.TicketId
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import io.github.oleksiybondar.api.http.routes.graphql.user.{UserApi, UserView}
import sangria.execution.UserFacingError
import sangria.schema.{
  Argument,
  BooleanType,
  Field,
  ListType,
  ObjectType,
  OptionInputType,
  OptionType,
  StringType,
  fields
}

import java.util.UUID
import scala.util.Try

object CommentApi {

  final case class InvalidCommentInput(override val getMessage: String)
      extends IllegalArgumentException(getMessage)
      with UserFacingError

  final case class CommentAccessDenied(override val getMessage: String)
      extends IllegalArgumentException(getMessage)
      with UserFacingError

  private val CommentIdArg        = Argument("commentId", StringType)
  private val TicketIdArg         = Argument("ticketId", StringType)
  private val UserIdArg           = Argument("userId", StringType)
  private val MessageArg          = Argument("message", StringType)
  private val RelatedCommentIdArg = Argument("relatedCommentId", OptionInputType(StringType))

  val CommentTicketSummaryType: ObjectType[GraphQLContext, CommentTicketSummaryView] =
    ObjectType(
      name = "CommentTicketSummaryView",
      fields[GraphQLContext, CommentTicketSummaryView](
        Field("id", StringType, resolve = _.value.id),
        Field("boardId", StringType, resolve = _.value.boardId),
        Field("title", StringType, resolve = _.value.title)
      )
    )

  val CommentType: ObjectType[GraphQLContext, CommentView] =
    ObjectType(
      name = "CommentView",
      fields[GraphQLContext, CommentView](
        Field("id", StringType, resolve = _.value.id),
        Field("ticketId", StringType, resolve = _.value.ticketId),
        Field("authorUserId", StringType, resolve = _.value.authorUserId),
        Field("createdAt", StringType, resolve = _.value.createdAt),
        Field("modifiedAt", StringType, resolve = _.value.modifiedAt),
        Field("message", StringType, resolve = _.value.message),
        Field("relatedCommentId", OptionType(StringType), resolve = _.value.relatedCommentId),
        Field(
          "user",
          OptionType(UserApi.UserType),
          resolve = ctx => loadUserView(ctx.ctx, ctx.value.authorUserId).unsafeToFuture()
        ),
        Field(
          "ticket",
          OptionType(CommentTicketSummaryType),
          resolve = ctx =>
            parseTicketId(ctx.value.ticketId) match {
              case Left(_)         => IO.pure(None).unsafeToFuture()
              case Right(ticketId) =>
                ctx.ctx.ticketService.getTicket(
                  ticketId
                ).map(_.map(toTicketSummaryView)).unsafeToFuture()
            }
        )
      )
    )

  val queryFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "comment",
        fieldType = OptionType(CommentType),
        arguments = CommentIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              commentId <- IO.fromEither(parseCommentId(ctx.arg(CommentIdArg)))
              comment   <- ctx.ctx.commentService.getComment(commentId)
              result    <- comment match {
                             case Some(value) =>
                               canReadComment(ctx.ctx, value, currentUserId).map {
                                 case true  => Some(toView(value))
                                 case false => None
                               }
                             case None        => IO.pure(None)
                           }
            } yield result
          }.unsafeToFuture()
      ),
      Field(
        name = "comments",
        fieldType = ListType(CommentType),
        arguments = TicketIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              ticketId <- IO.fromEither(parseTicketId(ctx.arg(TicketIdArg)))
              comments <- ctx.ctx.commentService.listComments(ticketId, currentUserId)
            } yield comments.map(toView)
          }.unsafeToFuture()
      ),
      Field(
        name = "commentsByUser",
        fieldType = ListType(CommentType),
        arguments = UserIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              userId   <- IO.fromEither(parseUserId(ctx.arg(UserIdArg)))
              comments <- ctx.ctx.commentService.listCommentsByUser(userId, currentUserId)
            } yield comments.map(toView)
          }.unsafeToFuture()
      )
    )

  val mutationFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "postComment",
        fieldType = CommentType,
        arguments = TicketIdArg :: MessageArg :: RelatedCommentIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              ticketId         <- IO.fromEither(parseTicketId(ctx.arg(TicketIdArg)))
              message          <- IO.fromEither(parseMessage(ctx.arg(MessageArg)))
              relatedCommentId <-
                IO.fromEither(parseOptionalCommentId(ctx.arg(RelatedCommentIdArg)))
              created          <- ctx.ctx.commentService.createComment(
                                    CreateCommentCommand(
                                      ticketId = ticketId,
                                      message = message,
                                      relatedCommentId = relatedCommentId
                                    ),
                                    currentUserId
                                  )
              comment          <- created.liftTo[IO](CommentAccessDenied("Comment could not be created"))
            } yield toView(comment)
          }.unsafeToFuture()
      ),
      Field(
        name = "updateCommentMessage",
        fieldType = CommentType,
        arguments = CommentIdArg :: MessageArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              commentId <- IO.fromEither(parseCommentId(ctx.arg(CommentIdArg)))
              message   <- IO.fromEither(parseMessage(ctx.arg(MessageArg)))
              updated   <- ctx.ctx.commentService.changeMessage(commentId, currentUserId, message)
              _         <-
                if (updated) IO.unit
                else IO.raiseError(CommentAccessDenied("Comment could not be updated"))
              comment   <- ctx.ctx.commentService.getComment(commentId).flatMap(
                             _.liftTo[IO](InvalidCommentInput("Comment was not found"))
                           )
            } yield toView(comment)
          }.unsafeToFuture()
      ),
      Field(
        name = "deleteComment",
        fieldType = BooleanType,
        arguments = CommentIdArg :: Nil,
        resolve = ctx =>
          withCurrentUser(ctx) { currentUserId =>
            for {
              commentId <- IO.fromEither(parseCommentId(ctx.arg(CommentIdArg)))
              deleted   <- ctx.ctx.commentService.deleteComment(commentId, currentUserId)
              _         <-
                if (deleted) IO.unit
                else IO.raiseError(CommentAccessDenied("Comment could not be deleted"))
            } yield true
          }.unsafeToFuture()
      )
    )

  private def canReadComment(
      ctx: GraphQLContext,
      comment: Comment,
      currentUserId: UserId
  ): IO[Boolean] =
    if (comment.authorUserId == currentUserId) IO.pure(true)
    else
      ctx.ticketService.getTicket(comment.ticketId).flatMap {
        case Some(ticket) =>
          ctx.dashboardAccessService.canReadComment(ticket.boardId, currentUserId)
        case None         => IO.pure(false)
      }

  private def loadUserView(ctx: GraphQLContext, rawUserId: String): IO[Option[UserView]] =
    parseUserId(rawUserId) match {
      case Left(_)       => IO.pure(None)
      case Right(userId) => ctx.userService.getUser(userId).map(_.map(toUserView))
    }

  private def withCurrentUser[A](
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  )(action: UserId => IO[A]): IO[A] =
    ctx.ctx.currentUserId match {
      case Some(currentUserId) => action(currentUserId)
      case None                => IO.raiseError(InvalidCommentInput("Authentication is required"))
    }

  private def parseCommentId(rawId: String): Either[InvalidCommentInput, CommentId] =
    Try(rawId.toLong).toEither.left.map(_ =>
      InvalidCommentInput(s"Invalid comment id: $rawId")
    ).map(CommentId(_))

  private def parseOptionalCommentId(rawId: Option[String])
      : Either[InvalidCommentInput, Option[CommentId]] =
    rawId.map(_.trim).filter(_.nonEmpty).traverse(parseCommentId)

  private def parseTicketId(rawId: String): Either[InvalidCommentInput, TicketId] =
    Try(
      rawId.toLong
    ).toEither.left.map(_ => InvalidCommentInput(s"Invalid ticket id: $rawId")).map(TicketId(_))

  private def parseUserId(rawId: String): Either[InvalidCommentInput, UserId] =
    Try(UUID.fromString(rawId)).toEither.left.map(_ =>
      InvalidCommentInput(s"Invalid UUID: $rawId")
    ).map(UserId(_))

  private def parseMessage(rawMessage: String): Either[InvalidCommentInput, CommentMessage] = {
    val normalized = rawMessage.trim
    Either.cond(
      normalized.nonEmpty,
      CommentMessage(normalized),
      InvalidCommentInput("Comment message is required")
    )
  }

  private def toView(comment: Comment): CommentView =
    CommentView(
      id = comment.id.value.toString,
      ticketId = comment.ticketId.value.toString,
      authorUserId = comment.authorUserId.value.toString,
      createdAt = comment.createdAt.toString,
      modifiedAt = comment.modifiedAt.toString,
      message = comment.message.value,
      relatedCommentId = comment.relatedCommentId.map(_.value.toString)
    )

  private def toTicketSummaryView(ticket: io.github.oleksiybondar.api.domain.ticket.Ticket)
      : CommentTicketSummaryView =
    CommentTicketSummaryView(
      id = ticket.id.value.toString,
      boardId = ticket.boardId.value.toString,
      title = ticket.name.value
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
