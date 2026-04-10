package io.github.oleksiybondar.api.http.routes.graphql.user

final case class UserView(
    id: String,
    username: Option[String],
    email: Option[String],
    firstName: String,
    lastName: String,
    avatarUrl: Option[String],
    createdAt: String
)

object UserView {
  def fromDomain(user: io.github.oleksiybondar.api.domain.user.User): UserView =
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
