package io.github.oleksiybondar.api.http.routes.graphql.dictionary

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.github.oleksiybondar.api.testkit.fixtures.{GraphQLFixtures, UserFixtures}
import io.github.oleksiybondar.api.testkit.support.GraphQLRequestSupport.graphqlRequest
import munit.FunSuite
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec._

class DictionaryGraphQLRoutesSpec extends FunSuite {

  test("POST /graphql returns ticket severities and time tracking activities") {
    val response = GraphQLFixtures.withGraphQLRoutes() { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(graphqlRequest(dictionaryQuery, accessToken = Some(token)))
      } yield response
    }

    val body                = response.as[Json].unsafeRunSync()
    val severitiesCursor    = body.hcursor.downField("data").downField("ticketSeverities")
    val activitiesCursor    = body.hcursor.downField("data").downField("timeTrackingActivities")
    val firstSeverityCursor = severitiesCursor.downArray
    val firstActivityCursor = activitiesCursor.downArray

    assertEquals(response.status, Status.Ok)
    assertEquals(firstSeverityCursor.get[String]("name").toOption, Some("minor"))
    assertEquals(
      firstSeverityCursor.get[String]("description").toOption,
      Some("Low impact issue or task.")
    )
    assertEquals(firstActivityCursor.get[String]("code").toOption, Some("code_review"))
    assertEquals(firstActivityCursor.get[String]("name").toOption, Some("Code Review"))
  }

  private val dictionaryQuery =
    """query {
      |  ticketSeverities {
      |    id
      |    name
      |    description
      |  }
      |  timeTrackingActivities {
      |    id
      |    code
      |    name
      |    description
      |  }
      |}""".stripMargin
}
