package io.github.oleksiybondar.api.http.routes.graphql.comment

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.testkit.fixtures.{
  BoardMemberFixtures,
  CommentFixtures,
  GraphQLFixtures,
  TicketFixtures,
  UserFixtures
}
import io.github.oleksiybondar.api.testkit.support.GraphQLRequestSupport.graphqlRequest
import munit.FunSuite
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec._

class CommentGraphQLRoutesSpec extends FunSuite {

  test("POST /graphql returns a single comment with nested user and ticket") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      users = List(UserFixtures.sampleUser),
      tickets = List(TicketFixtures.sampleTicket),
      comments = List(CommentFixtures.sampleComment)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(graphqlRequest(singleCommentQuery, accessToken = Some(token)))
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("comment")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("message").toOption, Some("This needs a follow-up review."))
    assertEquals(cursor.downField("user").get[String]("firstName").toOption, Some("Alice"))
    assertEquals(
      cursor.downField("ticket").get[String]("title").toOption,
      Some("Implement login mutation")
    )
  }

  test("POST /graphql returns ticket comments") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      tickets = List(TicketFixtures.sampleTicket),
      comments = List(CommentFixtures.sampleComment)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(graphqlRequest(ticketCommentsQuery, accessToken = Some(token)))
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("comments").downArray

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("message").toOption, Some("This needs a follow-up review."))
  }

  test("POST /graphql returns own comments by user") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      tickets = List(TicketFixtures.sampleTicket),
      comments = List(CommentFixtures.sampleComment)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(graphqlRequest(commentsByUserQuery, accessToken = Some(token)))
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("commentsByUser").downArray

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("id").toOption, Some("1"))
  }

  test("POST /graphql posts updates and deletes a comment") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      tickets = List(TicketFixtures.sampleTicket),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleId(1)))
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        _        <- ctx.httpApp.run(graphqlRequest(postCommentMutation, accessToken = Some(token)))
        _        <-
          ctx.httpApp.run(graphqlRequest(updateCommentMessageMutation, accessToken = Some(token)))
        response <-
          ctx.httpApp.run(graphqlRequest(deleteCommentMutation, accessToken = Some(token)))
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(body.hcursor.downField("data").get[Boolean]("deleteComment").toOption, Some(true))
  }

  test("POST /graphql returns empty for another user's comments") {
    val response = GraphQLFixtures.withGraphQLRoutes(
      tickets = List(TicketFixtures.sampleTicket),
      comments = List(CommentFixtures.sampleComment)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <-
          ctx.httpApp.run(graphqlRequest(otherUserCommentsQuery, accessToken = Some(token)))
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("commentsByUser")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.focus.flatMap(_.asArray).map(_.size), Some(0))
  }

  private val singleCommentQuery =
    """query {
      |  comment(commentId: "1") {
      |    id
      |    message
      |    user { firstName }
      |    ticket { id boardId title }
      |  }
      |}""".stripMargin

  private val ticketCommentsQuery =
    """query {
      |  comments(ticketId: "1") {
      |    id
      |    message
      |  }
      |}""".stripMargin

  private val commentsByUserQuery =
    """query {
      |  commentsByUser(userId: "11111111-1111-1111-1111-111111111111") {
      |    id
      |    message
      |  }
      |}""".stripMargin

  private val otherUserCommentsQuery =
    """query {
      |  commentsByUser(userId: "22222222-2222-2222-2222-222222222222") {
      |    id
      |  }
      |}""".stripMargin

  private val postCommentMutation =
    """mutation {
      |  postComment(ticketId: "1", message: "Posted from GraphQL") {
      |    id
      |    message
      |  }
      |}""".stripMargin

  private val updateCommentMessageMutation =
    """mutation {
      |  updateCommentMessage(commentId: "1", message: "Updated from GraphQL") {
      |    id
      |    message
      |  }
      |}""".stripMargin

  private val deleteCommentMutation =
    """mutation {
      |  deleteComment(commentId: "1")
      |}""".stripMargin
}
