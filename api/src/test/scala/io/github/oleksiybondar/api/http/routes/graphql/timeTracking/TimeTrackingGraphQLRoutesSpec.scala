package io.github.oleksiybondar.api.http.routes.graphql.timeTracking

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.{UserId, Username}
import io.github.oleksiybondar.api.testkit.fixtures.{
  BoardMemberFixtures,
  GraphQLFixtures,
  TicketFixtures,
  TimeTrackingFixtures,
  UserFixtures
}
import io.github.oleksiybondar.api.testkit.support.GraphQLRequestSupport.graphqlRequest
import munit.FunSuite
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec._

import java.util.UUID

class TimeTrackingGraphQLRoutesSpec extends FunSuite {

  test("POST /graphql returns time tracking entries by user with nested ticket and user") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      users = List(
        UserFixtures.sampleUser,
        UserFixtures.user(
          id = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
          username = Some(Username("bob"))
        )
      ),
      tickets = List(TicketFixtures.sampleTicket),
      timeEntries = List(TimeTrackingFixtures.sampleEntry)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(graphqlRequest(entriesByUserQuery, accessToken = Some(token)))
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor =
      body.hcursor.downField("data").downField("timeTrackingEntriesByUser").downArray

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[Int]("durationMinutes").toOption, Some(90))
    assertEquals(
      cursor.downField("ticket").get[String]("title").toOption,
      Some("Implement login mutation")
    )
    assertEquals(cursor.downField("user").get[String]("firstName").toOption, Some("Alice"))
  }

  test("POST /graphql returns ticket time entries for a readable ticket") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      tickets = List(TicketFixtures.sampleTicket),
      timeEntries = List(TimeTrackingFixtures.sampleEntry)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(graphqlRequest(entriesByTicketQuery, accessToken = Some(token)))
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor =
      body.hcursor.downField("data").downField("timeTrackingEntriesByTicket").downArray

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("id").toOption, Some("1"))
    assertEquals(
      cursor.get[String]("description").toOption,
      Some("Implemented GraphQL ticket queries.")
    )
  }

  test("POST /graphql returns a single time tracking entry") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      tickets = List(TicketFixtures.sampleTicket),
      timeEntries = List(TimeTrackingFixtures.sampleEntry)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(graphqlRequest(singleEntryQuery, accessToken = Some(token)))
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("timeTrackingEntry")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("id").toOption, Some("1"))
    assertEquals(
      cursor.downField("ticket").get[String]("title").toOption,
      Some("Implement login mutation")
    )
  }

  test("POST /graphql creates updates and deletes a time tracking entry") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      tickets = List(TicketFixtures.sampleTicket),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleId(3)))
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        _        <- ctx.httpApp.run(graphqlRequest(createEntryMutation, accessToken = Some(token)))
        _        <- ctx.httpApp.run(graphqlRequest(updateActivityMutation, accessToken = Some(token)))
        _        <- ctx.httpApp.run(
                      graphqlRequest(updateDescriptionMutation, accessToken = Some(token))
                    )
        _        <- ctx.httpApp.run(graphqlRequest(updateTimeMutation, accessToken = Some(token)))
        response <- ctx.httpApp.run(graphqlRequest(deleteEntryMutation, accessToken = Some(token)))
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(
      body.hcursor.downField("data").get[Boolean]("deleteTimeTrackingEntry").toOption,
      Some(true)
    )
  }

  test("POST /graphql rejects user time entry queries for another user") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      tickets = List(TicketFixtures.sampleTicket),
      timeEntries = List(TimeTrackingFixtures.sampleEntry)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(entriesByOtherUserQuery, accessToken = Some(token))
                    )
      } yield response
    }

    val body       = response.as[Json].unsafeRunSync()
    val dataCursor =
      body.hcursor.downField("data").downField("timeTrackingEntriesByUser")

    assertEquals(response.status, Status.Ok)
    assertEquals(dataCursor.focus.flatMap(_.asArray).map(_.size), Some(0))
  }

  private val entriesByUserQuery =
    """query {
      |  timeTrackingEntriesByUser(userId: "11111111-1111-1111-1111-111111111111") {
      |    id
      |    durationMinutes
      |    ticket { id title description }
      |    user { firstName }
      |  }
      |}""".stripMargin

  private val entriesByOtherUserQuery =
    """query {
      |  timeTrackingEntriesByUser(userId: "22222222-2222-2222-2222-222222222222") {
      |    id
      |  }
      |}""".stripMargin

  private val entriesByTicketQuery =
    """query {
      |  timeTrackingEntriesByTicket(ticketId: "1") {
      |    id
      |    description
      |  }
      |}""".stripMargin

  private val singleEntryQuery =
    """query {
      |  timeTrackingEntry(entryId: "1") {
      |    id
      |    description
      |    ticket { title }
      |    user { username }
      |  }
      |}""".stripMargin

  private val createEntryMutation =
    """mutation {
      |  createTimeTrackingEntry(
      |    ticketId: "1"
      |    activityId: 2
      |    durationMinutes: 30
      |    loggedAt: "2026-04-07T10:00:00Z"
      |    description: "Joined a planning meeting"
      |  ) {
      |    id
      |    durationMinutes
      |  }
      |}""".stripMargin

  private val updateActivityMutation =
    """mutation {
      |  updateTimeTrackingActivity(entryId: "1", activityId: 3) {
      |    id
      |    activityId
      |  }
      |}""".stripMargin

  private val updateDescriptionMutation =
    """mutation {
      |  updateTimeTrackingDescription(entryId: "1", description: "Updated meeting summary") {
      |    id
      |    description
      |  }
      |}""".stripMargin

  private val updateTimeMutation =
    """mutation {
      |  updateTimeTrackingTime(
      |    entryId: "1"
      |    durationMinutes: 60
      |    loggedAt: "2026-04-07T11:00:00Z"
      |  ) {
      |    id
      |    durationMinutes
      |    loggedAt
      |  }
      |}""".stripMargin

  private val deleteEntryMutation =
    """mutation {
      |  deleteTimeTrackingEntry(entryId: "1")
      |}""".stripMargin
}
