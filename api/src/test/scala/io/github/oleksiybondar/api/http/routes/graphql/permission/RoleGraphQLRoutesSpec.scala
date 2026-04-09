package io.github.oleksiybondar.api.http.routes.graphql.permission

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.github.oleksiybondar.api.testkit.fixtures.{GraphQLFixtures, UserFixtures}
import io.github.oleksiybondar.api.testkit.support.GraphQLRequestSupport.graphqlRequest
import munit.FunSuite
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec._

class RoleGraphQLRoutesSpec extends FunSuite {

  import GraphQLFixtures.withGraphQLRoutes

  test("POST /graphql returns seeded roles with permissions") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        rolesQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body  = response.as[Json].unsafeRunSync()
    val roles =
      body.hcursor
        .downField("data")
        .downField("roles")
        .focus
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assertEquals(roles.size, 3)
    assertEquals(roles.head.hcursor.get[String]("name").toOption, Some("admin"))
    assertEquals(
      roles.head.hcursor.downField("permissions").focus.flatMap(_.asArray).map(_.size),
      Some(3)
    )
  }

  test("POST /graphql returns a single role with nested permissions") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        roleQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("role")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("name").toOption, Some("admin"))
    assertEquals(
      cursor.downField("permissions").focus.flatMap(_.asArray).map(_.size),
      Some(3)
    )
  }

  test("POST /graphql returns null when the requested role does not exist") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        missingRoleQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(
      body.hcursor.downField("data").downField("role").focus,
      Some(Json.Null)
    )
  }

  private val rolesQuery =
    """query {
      |  roles {
      |    id
      |    name
      |    description
      |    permissions {
      |      id
      |      area
      |      canRead
      |      canCreate
      |      canModify
      |      canDelete
      |      canReassign
      |    }
      |  }
      |}""".stripMargin

  private val roleQuery =
    """query {
      |  role(id: 1) {
      |    id
      |    name
      |    permissions {
      |      id
      |    }
      |  }
      |}""".stripMargin

  private val missingRoleQuery =
    """query {
      |  role(id: 999) {
      |    id
      |    name
      |  }
      |}""".stripMargin
}
