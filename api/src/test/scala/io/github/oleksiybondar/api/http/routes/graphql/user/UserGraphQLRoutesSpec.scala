package io.github.oleksiybondar.api.http.routes.graphql.user

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.github.oleksiybondar.api.testkit.fixtures.{GraphQLFixtures, UserFixtures}
import io.github.oleksiybondar.api.testkit.support.GraphQLRequestSupport.graphqlRequest
import munit.FunSuite
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec.*

class UserGraphQLRoutesSpec extends FunSuite {

  import GraphQLFixtures.withGraphQLRoutes

  test("POST /graphql returns the requested user for a valid user query") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        _ <- ctx.seedAccessToken("valid-token", UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
          graphqlRequest(
            userQuery(UserFixtures.sampleUser.id.value.toString),
            accessToken = Some("valid-token")
          )
        )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("user")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("id").toOption, Some(UserFixtures.sampleUser.id.value.toString))
    assertEquals(cursor.get[Option[String]]("username").toOption.flatten, Some("alice"))
    assertEquals(cursor.get[Option[String]]("email").toOption.flatten, Some("alice@example.com"))
    assertEquals(cursor.get[String]("firstName").toOption, Some("Alice"))
    assertEquals(cursor.get[String]("lastName").toOption, Some("Example"))
  }

  test("POST /graphql returns null when the requested user does not exist") {
    val response = withGraphQLRoutes(Nil) { ctx =>
      for {
        _ <- ctx.seedAccessToken("valid-token", UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
          graphqlRequest(
            userQuery(UserFixtures.sampleUser.id.value.toString),
            accessToken = Some("valid-token")
          )
        )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(
      body.hcursor.downField("data").downField("user").focus,
      Some(Json.Null)
    )
  }

  test("POST /graphql returns an error when the user id is not a valid UUID") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        _ <- ctx.seedAccessToken("valid-token", UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
          graphqlRequest(
            userQuery("not-a-uuid"),
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

  private def userQuery(id: String): String =
    s"""query {
       |  user(id: "$id") {
       |    id
       |    username
       |    email
       |    firstName
       |    lastName
       |    avatarUrl
       |    createdAt
       |  }
       |}""".stripMargin
}
