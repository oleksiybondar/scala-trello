package io.github.oleksiybondar.api.http.routes.graphql.user

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.user.{UserId, UserMutationError}
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import sangria.execution.UserFacingError
import sangria.schema.{
  Argument,
  BooleanType,
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

/** User query and mutation fields exposed through the GraphQL schema. */
object UserApi {

  /** User-facing GraphQL error used for invalid input and rejected mutations. */
  final case class InvalidUserInput(override val getMessage: String)
      extends IllegalArgumentException(getMessage)
      with UserFacingError

  /** Shared GraphQL view of a user returned by queries and user mutations. */
  val UserType: ObjectType[Unit, UserView] =
    ObjectType(
      name = "UserView",
      fields[Unit, UserView](
        Field("id", StringType, resolve = _.value.id),
        Field("username", OptionType(StringType), resolve = _.value.username),
        Field("email", OptionType(StringType), resolve = _.value.email),
        Field("firstName", StringType, resolve = _.value.firstName),
        Field("lastName", StringType, resolve = _.value.lastName),
        Field("avatarUrl", OptionType(StringType), resolve = _.value.avatarUrl),
        Field("createdAt", StringType, resolve = _.value.createdAt)
      )
    )

  private val IdArg              = Argument("id", StringType)
  private val OffsetArg          = Argument("offset", IntType, defaultValue = 0)
  private val LimitArg           = Argument("limit", IntType, defaultValue = 20)
  private val FirstNameArg       = Argument("firstName", StringType)
  private val LastNameArg        = Argument("lastName", StringType)
  private val AvatarUrlArg       = Argument("avatarUrl", OptionInputType(StringType))
  private val UsernameArg        = Argument("username", StringType)
  private val EmailArg           = Argument("email", StringType)
  private val CurrentPasswordArg = Argument("currentPassword", StringType)
  private val NewPasswordArg     = Argument("newPassword", StringType)

  val queryFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "user",
        fieldType = OptionType(UserType),
        arguments = IdArg :: Nil,
        resolve = ctx => {
          IO
            .fromEither(parseUserId(ctx.arg(IdArg)))
            .flatMap(userId => ctx.ctx.userService.getUser(userId).map(_.map(toView)))
            .unsafeToFuture()
        }
      ),
      Field(
        name = "users",
        fieldType = ListType(UserType),
        arguments = OffsetArg :: LimitArg :: Nil,
        resolve = ctx => {
          val offset = math.max(0, ctx.arg(OffsetArg))
          val limit  = math.max(0, ctx.arg(LimitArg))

          ctx.ctx.userService
            .listUsersPage(offset, limit)
            .map(_.map(toView))
            .unsafeToFuture()
        }
      )
    )

  /** Mutations that operate only on the currently authenticated user. */
  val mutationFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "updateProfile",
        fieldType = UserType,
        arguments = FirstNameArg :: LastNameArg :: Nil,
        resolve = (ctx: sangria.schema.Context[GraphQLContext, Unit]) =>
          withCurrentUser(ctx) { userId =>
            liftUserMutation(
              ctx.ctx.userService
                .updateProfile(userId, ctx.arg(FirstNameArg), ctx.arg(LastNameArg))
                .map(toView)
            )
          }.unsafeToFuture()
      ),
      Field(
        name = "changeAvatar",
        fieldType = UserType,
        arguments = AvatarUrlArg :: Nil,
        resolve = (ctx: sangria.schema.Context[GraphQLContext, Unit]) =>
          withCurrentUser(ctx) { userId =>
            liftUserMutation(
              ctx.ctx.userService
                .changeAvatar(userId, ctx.arg(AvatarUrlArg))
                .map(toView)
            )
          }.unsafeToFuture()
      ),
      Field(
        name = "changeUsername",
        fieldType = UserType,
        arguments = UsernameArg :: Nil,
        resolve = (ctx: sangria.schema.Context[GraphQLContext, Unit]) =>
          withCurrentUser(ctx) { userId =>
            liftUserMutation(
              ctx.ctx.userService
                .changeUsername(userId, ctx.arg(UsernameArg))
                .map(toView)
            )
          }.unsafeToFuture()
      ),
      Field(
        name = "changeEmail",
        fieldType = UserType,
        arguments = EmailArg :: Nil,
        resolve = (ctx: sangria.schema.Context[GraphQLContext, Unit]) =>
          withCurrentUser(ctx) { userId =>
            liftUserMutation(
              ctx.ctx.userService
                .changeEmail(userId, ctx.arg(EmailArg))
                .map(toView)
            )
          }.unsafeToFuture()
      ),
      Field(
        name = "changePassword",
        fieldType = BooleanType,
        arguments = CurrentPasswordArg :: NewPasswordArg :: Nil,
        resolve = (ctx: sangria.schema.Context[GraphQLContext, Unit]) =>
          withCurrentUser(ctx) { userId =>
            liftUserMutation(
              ctx.ctx.userService
                .changePassword(userId, ctx.arg(CurrentPasswordArg), ctx.arg(NewPasswordArg))
            )
          }.unsafeToFuture()
      )
    )

  private def parseUserId(rawId: String): Either[InvalidUserInput, UserId] =
    Try(UUID.fromString(rawId))
      .toEither
      .left
      .map(_ => InvalidUserInput(s"Invalid UUID: $rawId"))
      .map(UserId(_))

  private def withCurrentUser[A](
      ctx: sangria.schema.Context[GraphQLContext, Unit]
  )(run: UserId => IO[A]): IO[A] =
    ctx.ctx.currentUserId match {
      case Some(userId) => run(userId)
      case None         => IO.raiseError(InvalidUserInput("Authentication context is missing"))
    }

  private def toUserFacingError(error: UserMutationError): InvalidUserInput =
    error match {
      case UserMutationError.UserNotFound           => InvalidUserInput("Current user was not found")
      case UserMutationError.UsernameRequired       => InvalidUserInput("Username is required")
      case UserMutationError.UsernameAlreadyUsed    => InvalidUserInput("Username is already in use")
      case UserMutationError.EmailRequired          => InvalidUserInput("Email is required")
      case UserMutationError.InvalidEmail           => InvalidUserInput("Email is invalid")
      case UserMutationError.EmailAlreadyUsed       => InvalidUserInput("Email is already in use")
      case UserMutationError.InvalidCurrentPassword =>
        InvalidUserInput("Current password is invalid")
      case UserMutationError.PasswordAlreadyUsed    =>
        InvalidUserInput("New password must not reuse a previous password")
      case UserMutationError.WeakPassword(_)        =>
        InvalidUserInput("Password does not satisfy the strength requirements")
    }

  private def liftUserMutation[A](
      action: cats.data.EitherT[IO, UserMutationError, A]
  ): IO[A] =
    action.value.flatMap {
      case Right(value) => IO.pure(value)
      case Left(error)  => IO.raiseError(toUserFacingError(error))
    }

  private def toView(user: io.github.oleksiybondar.api.domain.user.User): UserView =
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
