package io.github.oleksiybondar.api.http.routes.graphql.user

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import sangria.execution.UserFacingError
import sangria.schema.{Argument, Field, ObjectType, OptionType, StringType, fields}

import java.util.UUID
import scala.util.Try

object UserApi {

  final case class InvalidUserInput(override val getMessage: String)
      extends IllegalArgumentException(getMessage)
      with UserFacingError

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

  private val IdArg = Argument("id", StringType)

  val queryFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "user",
        fieldType = OptionType(UserType),
        arguments = IdArg :: Nil,
        resolve = ctx => {
          IO
            .fromEither(parseUserId(ctx.arg(IdArg)))
            .flatMap(userId => ctx.ctx.userRepo.findById(userId).map(_.map(toView)))
            .unsafeToFuture()
        }
      )
    )

  private def parseUserId(rawId: String): Either[InvalidUserInput, UserId] =
    Try(UUID.fromString(rawId))
      .toEither
      .left
      .map(_ => InvalidUserInput(s"Invalid UUID: $rawId"))
      .map(UserId(_))

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
