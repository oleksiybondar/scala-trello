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
}
