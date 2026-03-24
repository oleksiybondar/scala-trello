package io.github.oleksiybondar.api.http.routes.graphql.user

import caliban.schema.Schema
import caliban.schema.SchemaDerivation

object UserSchema extends SchemaDerivation[Any] {

  final case class UserView(
                             id: String,
                             username: Option[String],
                             email: Option[String],
                             firstName: String,
                             lastName: String,
                             avatarUrl: Option[String],
                             createdAt: String
                           ) derives Schema.SemiAuto
}