package io.github.oleksiybondar.api.http.routes.graphql.ticket

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.{UserId, Username}
import io.github.oleksiybondar.api.testkit.fixtures.{
  BoardMemberFixtures,
  CommentFixtures,
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

class TicketGraphQLRoutesSpec extends FunSuite {

  test("POST /graphql returns a single ticket with nested createdBy and assignedTo users") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      users = List(
        UserFixtures.sampleUser,
        UserFixtures.user(
          id = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
          username = Some(Username("bob"))
        )
      ),
      tickets = List(TicketFixtures.sampleTicket)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(graphqlRequest(ticketQuery, accessToken = Some(token)))
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("ticket")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("name").toOption, Some("Implement login mutation"))
    assertEquals(cursor.get[String]("status").toOption, Some("new"))
    assertEquals(cursor.get[Int]("estimatedMinutes").toOption, Some(120))
    assertEquals(cursor.downField("createdBy").get[String]("firstName").toOption, Some("Alice"))
    assertEquals(cursor.downField("assignedTo").get[String]("username").toOption, Some("bob"))
  }

  test("POST /graphql resolves nested board tickets with status and estimated minutes") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      tickets = List(TicketFixtures.sampleTicket)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(graphqlRequest(boardTicketsQuery, accessToken = Some(token)))
      } yield response
    }

    val body         = response.as[Json].unsafeRunSync()
    val ticketCursor =
      body.hcursor.downField("data").downField("board").downField("tickets").downArray

    assertEquals(response.status, Status.Ok)
    assertEquals(ticketCursor.get[String]("name").toOption, Some("Implement login mutation"))
    assertEquals(ticketCursor.get[String]("status").toOption, Some("new"))
    assertEquals(ticketCursor.get[Int]("estimatedMinutes").toOption, Some(120))
  }

  test("POST /graphql returns nested ticket comments and time entries") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      tickets = List(TicketFixtures.sampleTicket),
      comments = List(CommentFixtures.sampleComment),
      timeEntries = List(TimeTrackingFixtures.sampleEntry)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <-
          ctx.httpApp.run(graphqlRequest(ticketNestedActivityQuery, accessToken = Some(token)))
      } yield response
    }

    val body               = response.as[Json].unsafeRunSync()
    val ticketCursor       = body.hcursor.downField("data").downField("ticket")
    val commentCursor      = ticketCursor.downField("comments").downArray
    val timeTrackingCursor = ticketCursor.downField("timeEntries").downArray

    assertEquals(response.status, Status.Ok)
    assertEquals(
      commentCursor.get[String]("message").toOption,
      Some("This needs a follow-up review.")
    )
    assertEquals(commentCursor.downField("user").get[String]("firstName").toOption, Some("Alice"))
    assertEquals(timeTrackingCursor.get[Int]("durationMinutes").toOption, Some(90))
    assertEquals(
      timeTrackingCursor.downField("user").get[String]("firstName").toOption,
      Some("Alice")
    )
    assertEquals(
      timeTrackingCursor.downField("ticket").get[String]("title").toOption,
      Some("Implement login mutation")
    )
  }

  test("POST /graphql creates a ticket and returns it") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      users = List(
        UserFixtures.sampleUser,
        UserFixtures.user(
          id = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
          username = Some(Username("bob"))
        )
      ),
      members = List(
        BoardMemberFixtures.sampleMember,
        BoardMemberFixtures.member(
          userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
          roleId = RoleId(2)
        )
      )
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(graphqlRequest(createTicketMutation, accessToken = Some(token)))
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("createTicket")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("name").toOption, Some("New GraphQL ticket"))
    assertEquals(cursor.get[String]("status").toOption, Some("new"))
    assertEquals(cursor.get[Int]("estimatedMinutes").toOption, Some(45))
    assertEquals(
      cursor.get[String]("assignedToUserId").toOption,
      Some("22222222-2222-2222-2222-222222222222")
    )
  }

  test("POST /graphql updates ticket fields and reassigns the ticket") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      users = List(
        UserFixtures.sampleUser,
        UserFixtures.user(
          id = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
          username = Some(Username("bob"))
        )
      ),
      tickets = List(TicketFixtures.sampleTicket),
      members = List(
        BoardMemberFixtures.sampleMember,
        BoardMemberFixtures.member(
          userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
          roleId = RoleId(2)
        )
      )
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        _        <- ctx.httpApp.run(graphqlRequest(changeTitleMutation, accessToken = Some(token)))
        _        <- ctx.httpApp.run(
                      graphqlRequest(changeDescriptionMutation, accessToken = Some(token))
                    )
        _        <- ctx.httpApp.run(
                      graphqlRequest(changeAcceptanceMutation, accessToken = Some(token))
                    )
        _        <- ctx.httpApp.run(
                      graphqlRequest(changeEstimatedMutation, accessToken = Some(token))
                    )
        response <- ctx.httpApp.run(graphqlRequest(reassignMutation, accessToken = Some(token)))
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("reassignTicket")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("name").toOption, Some("Renamed ticket"))
    assertEquals(cursor.get[String]("description").toOption, Some("Updated description"))
    assertEquals(
      cursor.get[String]("acceptanceCriteria").toOption,
      Some("Updated acceptance criteria")
    )
    assertEquals(cursor.get[Int]("estimatedMinutes").toOption, Some(240))
    assertEquals(
      cursor.get[String]("assignedToUserId").toOption,
      Some("22222222-2222-2222-2222-222222222222")
    )
  }

  test("POST /graphql rejects reassignment when the target user is not a board member") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      tickets = List(TicketFixtures.sampleTicket)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(reassignMissingMemberMutation, accessToken = Some(token))
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val errors =
      body.hcursor.downField("errors").focus.flatMap(_.asArray).getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assert(errors.nonEmpty)
    assertEquals(
      errors.head.hcursor.get[String]("message").toOption,
      Some("Ticket could not be reassigned")
    )
  }

  private val ticketQuery =
    """query {
      |  ticket(ticketId: "1") {
      |    id
      |    name
      |    status
      |    estimatedMinutes
      |    createdBy { firstName }
      |    assignedTo { username }
      |  }
      |}""".stripMargin

  private val boardTicketsQuery =
    """query {
      |  board(boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") {
      |    id
      |    tickets {
      |      name
      |      status
      |      estimatedMinutes
      |    }
      |  }
      |}""".stripMargin

  private val ticketNestedActivityQuery =
    """query {
      |  ticket(ticketId: "1") {
      |    id
      |    comments {
      |      message
      |      user { firstName }
      |      ticket { title }
      |    }
      |    timeEntries {
      |      durationMinutes
      |      user { firstName }
      |      ticket { title }
      |    }
      |  }
      |}""".stripMargin

  private val createTicketMutation =
    """mutation {
      |  createTicket(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    title: "New GraphQL ticket"
      |    description: "Created from GraphQL"
      |    acceptanceCriteria: "It should work"
      |    estimatedMinutes: 45
      |    assignedToUserId: "22222222-2222-2222-2222-222222222222"
      |  ) {
      |    id
      |    name
      |    status
      |    estimatedMinutes
      |    assignedToUserId
      |  }
      |}""".stripMargin

  private val changeTitleMutation =
    """mutation {
      |  changeTicketTitle(ticketId: "1", title: "Renamed ticket") {
      |    id
      |    name
      |  }
      |}""".stripMargin

  private val changeDescriptionMutation =
    """mutation {
      |  changeTicketDescription(ticketId: "1", description: "Updated description") {
      |    id
      |    description
      |  }
      |}""".stripMargin

  private val changeAcceptanceMutation =
    """mutation {
      |  changeTicketAcceptanceCriteria(ticketId: "1", acceptanceCriteria: "Updated acceptance criteria") {
      |    id
      |    acceptanceCriteria
      |  }
      |}""".stripMargin

  private val changeEstimatedMutation =
    """mutation {
      |  changeTicketEstimatedTime(ticketId: "1", estimatedMinutes: 240) {
      |    id
      |    estimatedMinutes
      |  }
      |}""".stripMargin

  private val reassignMutation =
    """mutation {
      |  reassignTicket(ticketId: "1", assignedToUserId: "22222222-2222-2222-2222-222222222222") {
      |    id
      |    name
      |    description
      |    acceptanceCriteria
      |    estimatedMinutes
      |    assignedToUserId
      |  }
      |}""".stripMargin

  private val reassignMissingMemberMutation =
    """mutation {
      |  reassignTicket(ticketId: "1", assignedToUserId: "33333333-3333-3333-3333-333333333333") {
      |    id
      |  }
      |}""".stripMargin
}
