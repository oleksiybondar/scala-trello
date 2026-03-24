package io.github.oleksiybondar.api.http.routes

import cats.effect.Concurrent
import cats.syntax.all.*
import io.circe.generic.auto.*
import io.github.oleksiybondar.api.domain.user.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl

import java.util.UUID
import scala.util.Try

object UserRoutes {

  final case class UserResponse(
                                 id: UUID,
                                 username: Option[String],
                                 email: Option[String],
                                 firstName: String,
                                 lastName: String,
                                 avatarUrl: Option[String],
                                 createdAt: String
                               )

  private def toResponse(user: User): UserResponse =
    UserResponse(
      id = user.id.value,
      username = user.username.map(_.value),
      email = user.email.map(_.value),
      firstName = user.firstName.value,
      lastName = user.lastName.value,
      avatarUrl = user.avatarUrl.map(_.value),
      createdAt = user.createdAt.toString
    )

  private def parseUserId(raw: String): Option[UserId] =
    Try(UUID.fromString(raw)).toOption.map(UserId(_))

  def routes[F[_]: Concurrent](userService: UserService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / rawId =>
        parseUserId(rawId) match {
          case None =>
            BadRequest("Invalid user id")

          case Some(userId) =>
            userService.getUser(userId).flatMap {
              case Some(user) => Ok(toResponse(user))
              case None       => NotFound()
            }
        }
    }
  }
}