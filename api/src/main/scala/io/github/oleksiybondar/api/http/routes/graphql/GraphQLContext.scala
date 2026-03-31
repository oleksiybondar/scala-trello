package io.github.oleksiybondar.api.http.routes.graphql

import cats.effect.IO
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo

final case class GraphQLContext(
    userRepo: UserRepo[IO]
)
