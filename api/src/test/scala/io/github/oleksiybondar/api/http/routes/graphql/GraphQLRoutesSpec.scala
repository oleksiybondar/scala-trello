package io.github.oleksiybondar.api.http.routes.graphql

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import io.github.oleksiybondar.api.testkit.fixtures.GraphQLFixtures.withGraphQLRoutes
import io.github.oleksiybondar.api.testkit.support.GraphQLRequestSupport.graphqlRequest
import munit.FunSuite
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec.*

class GraphQLRoutesSpec extends FunSuite {

  test("POST /graphql returns unauthorized when the Authorization header is missing") {
    val response = withGraphQLRoutes() { ctx =>
      ctx.httpApp.run(graphqlRequest(introspectionQuery))
    }

    val body = response.bodyText.compile.string.unsafeRunSync()

    assertEquals(response.status, Status.Unauthorized)
    assertEquals(body, "Missing or invalid Authorization header")
  }

  test("POST /graphql serves the schema for an authorized request") {
    val response = withGraphQLRoutes() { ctx =>
      for {
        _ <- ctx.seedAccessToken("valid-token", UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
          graphqlRequest(
            introspectionQuery,
            accessToken = Some("valid-token")
          )
        )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()
    val cursor =
      body.hcursor.downField("data").downField("__schema").downField("queryType")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("name").toOption, Some("Queries"))
  }

  test("POST /graphql returns GraphQL errors for an invalid schema query") {
    val response = withGraphQLRoutes() { ctx =>
      for {
        _ <- ctx.seedAccessToken("valid-token", UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
          graphqlRequest(
            invalidSchemaQuery,
            accessToken = Some("valid-token")
          )
        )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()
    val errors =
      body.hcursor
        .downField("errors")
        .focus
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assert(errors.nonEmpty)
  }

  private val introspectionQuery =
    """query {
      |  __schema {
      |    queryType {
      |      name
      |    }
      |  }
      |}""".stripMargin

  private val invalidSchemaQuery =
    """query {
      |  missingField
      |}""".stripMargin
}
