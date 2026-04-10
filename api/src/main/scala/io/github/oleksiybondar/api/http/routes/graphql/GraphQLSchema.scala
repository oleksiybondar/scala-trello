package io.github.oleksiybondar.api.http.routes.graphql

import io.github.oleksiybondar.api.http.routes.graphql.board.BoardApi
import io.github.oleksiybondar.api.http.routes.graphql.comment.CommentApi
import io.github.oleksiybondar.api.http.routes.graphql.permission.RoleApi
import io.github.oleksiybondar.api.http.routes.graphql.ticket.TicketApi
import io.github.oleksiybondar.api.http.routes.graphql.timeTracking.TimeTrackingApi
import io.github.oleksiybondar.api.http.routes.graphql.user.UserApi
import sangria.schema.{ObjectType, Schema, fields}

object GraphQLSchema {

  // New GraphQL modules plug into the root schema here.
  private val QueryType: ObjectType[GraphQLContext, Unit] =
    ObjectType(
      name = "Queries",
      fields = fields[GraphQLContext, Unit](
        (
          UserApi.queryFields ++
            RoleApi.queryFields ++
            BoardApi.queryFields ++
            TicketApi.queryFields ++
            TimeTrackingApi.queryFields ++
            CommentApi.queryFields
        )*
      )
    )

  private val MutationType: ObjectType[GraphQLContext, Unit] =
    ObjectType(
      name = "Mutations",
      fields = fields[GraphQLContext, Unit](
        (UserApi.mutationFields ++ BoardApi.mutationFields ++ TicketApi.mutationFields ++ TimeTrackingApi.mutationFields ++ CommentApi.mutationFields)*
      )
    )

  val schema: Schema[GraphQLContext, Unit] =
    Schema(query = QueryType, mutation = Some(MutationType))
}
