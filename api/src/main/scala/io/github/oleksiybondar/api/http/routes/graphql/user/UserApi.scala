package io.github.oleksiybondar.api.http.routes.graphql.user

import caliban.*
import caliban.interop.cats.implicits.*
import caliban.schema.{ArgBuilder, Schema, SchemaDerivation}
import cats.effect.IO
import cats.effect.std.Dispatcher
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo
import io.github.oleksiybondar.api.http.routes.graphql.user.UserSchema.*

import java.util.UUID
import scala.util.Try

object UserApi extends SchemaDerivation[Any] {

  final case class UserArgs(id: String) derives Schema.SemiAuto, ArgBuilder

  def api(userRepo: UserRepo[IO])(using Dispatcher[IO]): GraphQL[Any] = {

    final case class Queries(
                              user: UserArgs => IO[Option[UserView]]
                            ) derives Schema.SemiAuto

    val queries =
      Queries(
        user = args =>
          IO.fromEither(
            Try(UUID.fromString(args.id))
              .toEither
              .left
              .map(e => new RuntimeException(s"Invalid UUID: ${args.id}", e))
          ).flatMap { uuid =>
            userRepo.findById(UserId(uuid)).map(_.map(toView))
          }
      )

    graphQL(
      RootResolver(queries)
    )
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