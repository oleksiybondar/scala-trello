package io.github.oleksiybondar.api.http.docs.rest

import io.circe.Json
import io.github.oleksiybondar.api.http.docs.graphql.GraphiQLRoutes
import io.github.oleksiybondar.api.http.routes.rest.auth.AuthRoutes
import io.github.oleksiybondar.api.http.routes.rest.health.HealthRoutes
import sttp.tapir.json.circe._
import sttp.tapir.{AnyEndpoint, _}

object ApiSpec {

  private val graphqlEndpoint: AnyEndpoint =
    endpoint.post
      .in("graphql")
      .securityIn(
        sttp.tapir.auth.bearer[String]()
          .description("Access token. Use the GraphiQL IDE at /docs/graphql.")
      )
      .in(jsonBody[Json])
      .out(jsonBody[Json])
      .name("graphql")
      .description("Protected GraphQL API endpoint")
      .tag("graphql")

  val auth: List[AnyEndpoint]   = AuthRoutes.all
  val health: List[AnyEndpoint] = HealthRoutes.all

  val graphql: List[AnyEndpoint] =
    List(
      graphqlEndpoint
    )

  val graphiql: List[AnyEndpoint] = GraphiQLRoutes.all

  val all: List[AnyEndpoint] =
    auth ++ health ++ graphql ++ graphiql
}
