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
