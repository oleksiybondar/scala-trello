package io.github.oleksiybondar.api.http.routes.graphql

import io.github.oleksiybondar.api.http.routes.graphql.dashboard.DashboardApi
import io.github.oleksiybondar.api.http.routes.graphql.permission.RoleApi
import io.github.oleksiybondar.api.http.routes.graphql.user.UserApi
import sangria.schema.{ObjectType, Schema, fields}

object GraphQLSchema {

  // New GraphQL modules plug into the root schema here.
  private val QueryType: ObjectType[GraphQLContext, Unit] =
    ObjectType(
      name = "Queries",
      fields = fields[GraphQLContext, Unit](
        (UserApi.queryFields ++ RoleApi.queryFields ++ DashboardApi.queryFields)*
      )
    )

  private val MutationType: ObjectType[GraphQLContext, Unit] =
    ObjectType(
      name = "Mutations",
      fields = fields[GraphQLContext, Unit](
        (UserApi.mutationFields ++ DashboardApi.mutationFields)*
      )
    )

  val schema: Schema[GraphQLContext, Unit] =
    Schema(query = QueryType, mutation = Some(MutationType))
}
