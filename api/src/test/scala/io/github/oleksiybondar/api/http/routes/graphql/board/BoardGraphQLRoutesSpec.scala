package io.github.oleksiybondar.api.http.routes.graphql.board

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.github.oleksiybondar.api.testkit.fixtures.{GraphQLFixtures, UserFixtures}
import io.github.oleksiybondar.api.testkit.support.GraphQLRequestSupport.graphqlRequest
import munit.FunSuite
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec._

class BoardGraphQLRoutesSpec extends FunSuite {

  import GraphQLFixtures.withGraphQLRoutes

  test("POST /graphql returns boards for the current user based on membership") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        myBoardsQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body       = response.as[Json].unsafeRunSync()
    val dashboards =
      body.hcursor
        .downField("data")
        .downField("myBoards")
        .focus
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assertEquals(dashboards.size, 1)
    assertEquals(
      dashboards.head.hcursor.get[String]("id").toOption,
      Some("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    )
    assertEquals(
      dashboards.head.hcursor.get[String]("name").toOption,
      Some("Core Board")
    )
  }

  test("POST /graphql creates a board and returns it") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        createBoardMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("createBoard")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("name").toOption, Some("Platform Board"))
    assertEquals(cursor.get[Boolean]("active").toOption, Some(true))
  }

  test("POST /graphql supports querying dashboards together with members and roles") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        dashboardsAndMembersQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body       = response.as[Json].unsafeRunSync()
    val dashboards =
      body.hcursor
        .downField("data")
        .downField("myBoards")
        .focus
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)
    val members    =
      body.hcursor
        .downField("data")
        .downField("dashboardMembers")
        .focus
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assertEquals(dashboards.size, 1)
    assertEquals(members.size, 2)
    assertEquals(
      dashboards.head.hcursor.get[String]("id").toOption,
      Some("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    )
    assertEquals(
      members.head.hcursor.downField("role").get[String]("name").toOption,
      Some("admin")
    )
  }

  test("POST /graphql resolves nested board users, members count, and member role permissions") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        nestedBoardDataQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body            = response.as[Json].unsafeRunSync()
    val boardCursor     = body.hcursor.downField("data").downField("myBoards").downArray
    val memberCursor    = body.hcursor.downField("data").downField("dashboardMembers").downArray
    val permissionsJson =
      memberCursor
        .downField("role")
        .downField("permissions")
        .focus
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assertEquals(
      boardCursor.get[String]("id").toOption,
      Some("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    )
    assertEquals(boardCursor.downField("owner").get[String]("firstName").toOption, Some("Alice"))
    assertEquals(
      boardCursor.downField("createdBy").get[String]("lastName").toOption,
      Some("Example")
    )
    assertEquals(
      boardCursor.downField("lastModifiedBy").get[String]("id").toOption,
      Some(UserFixtures.sampleUser.id.value.toString)
    )
    assertEquals(boardCursor.get[Int]("membersCount").toOption, Some(2))
    assertEquals(
      memberCursor.get[String]("boardId").toOption,
      Some("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    )
    assertEquals(
      memberCursor.get[String]("dashboardId").toOption,
      Some("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    )
    assertEquals(memberCursor.downField("role").get[String]("name").toOption, Some("admin"))
    assertEquals(permissionsJson.size, 3)
    assertEquals(
      permissionsJson.head.hcursor.get[String]("area").toOption,
      Some("dashboard")
    )
  }

  test("POST /graphql returns dashboard members for an authorized dashboard member") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        dashboardMembersQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body    = response.as[Json].unsafeRunSync()
    val members =
      body.hcursor
        .downField("data")
        .downField("dashboardMembers")
        .focus
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assertEquals(members.size, 2)
    assertEquals(
      members.head.hcursor.get[String]("userId").toOption,
      Some(UserFixtures.sampleUser.id.value.toString)
    )
    assertEquals(
      members.head.hcursor.downField("role").get[String]("name").toOption,
      Some("admin")
    )
  }

  test("POST /graphql deactivates a dashboard") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        deactivateDashboardMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("deactivateDashboard")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[Boolean]("active").toOption, Some(false))
  }

  test("POST /graphql invites a dashboard member") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        inviteDashboardMemberMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("inviteDashboardMember")

    assertEquals(response.status, Status.Ok)
    assertEquals(
      cursor.get[String]("userId").toOption,
      Some("44444444-4444-4444-4444-444444444444")
    )
    assertEquals(
      cursor.downField("role").get[String]("name").toOption,
      Some("viewer")
    )
  }

  test("POST /graphql returns an error when inviting a user who is already a dashboard member") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        duplicateInviteDashboardMemberMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Member could not be added to the dashboard"))
  }

  test("POST /graphql changes a dashboard member role") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeDashboardMemberRoleMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("changeDashboardMemberRole")

    assertEquals(response.status, Status.Ok)
    assertEquals(
      cursor.downField("role").get[String]("name").toOption,
      Some("viewer")
    )
  }

  test("POST /graphql removes a dashboard member") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        removeDashboardMemberMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(
      body.hcursor.downField("data").get[Boolean]("removeDashboardMember").toOption,
      Some(true)
    )
  }

  test("POST /graphql returns an error when the current user cannot read the dashboard") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(
                      io.github.oleksiybondar.api.domain.user.UserId(
                        java.util.UUID.fromString("99999999-9999-9999-9999-999999999999")
                      )
                    )
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        dashboardMembersQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("You do not have access to this dashboard"))
  }

  test("POST /graphql still supports the legacy myDashboards alias") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        myDashboardsLegacyQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body       = response.as[Json].unsafeRunSync()
    val dashboards =
      body.hcursor
        .downField("data")
        .downField("myDashboards")
        .focus
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assertEquals(dashboards.size, 1)
  }

  test("POST /graphql still supports the legacy createDashboard alias") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        createDashboardLegacyMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("createDashboard")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("name").toOption, Some("Platform Board"))
    assertEquals(cursor.get[Boolean]("active").toOption, Some(true))
  }

  test("POST /graphql supports the legacy createDashboard alias with a null description") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        createDashboardLegacyMutationWithNullDescription,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("createDashboard")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("name").toOption, Some("demo board"))
    assertEquals(cursor.get[Option[String]]("description").toOption.flatten, None)
    assertEquals(body.hcursor.downField("errors").focus, None)
  }

  private val dashboardMembersQuery =
    """query {
      |  dashboardMembers(dashboardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") {
      |    dashboardId
      |    userId
      |    createdAt
      |    role {
      |      id
      |      name
      |      description
      |    }
      |  }
      |}""".stripMargin

  private val myBoardsQuery =
    """query {
      |  myBoards {
      |    id
      |    name
      |    active
      |  }
      |}""".stripMargin

  private val myDashboardsLegacyQuery =
    """query {
      |  myDashboards {
      |    id
      |    name
      |    active
      |  }
      |}""".stripMargin

  private val createBoardMutation =
    """mutation {
      |  createBoard(name: "Platform Board", description: "Platform work") {
      |    id
      |    name
      |    active
      |  }
      |}""".stripMargin

  private val createDashboardLegacyMutation =
    """mutation {
      |  createDashboard(name: "Platform Board", description: "Platform work") {
      |    id
      |    name
      |    active
      |  }
      |}""".stripMargin

  private val createDashboardLegacyMutationWithNullDescription =
    """mutation {
      |  createDashboard(name: "demo board", description: null) {
      |    id
      |    name
      |    description
      |    active
      |    ownerUserId
      |    createdByUserId
      |    createdAt
      |    modifiedAt
      |    lastModifiedByUserId
      |  }
      |}""".stripMargin

  private val deactivateDashboardMutation =
    """mutation {
      |  deactivateDashboard(dashboardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") {
      |    id
      |    active
      |  }
      |}""".stripMargin

  private val inviteDashboardMemberMutation =
    """mutation {
      |  inviteDashboardMember(
      |    dashboardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    userId: "44444444-4444-4444-4444-444444444444"
      |    roleId: 3
      |  ) {
      |    dashboardId
      |    userId
      |    role {
      |      id
      |      name
      |    }
      |  }
      |}""".stripMargin

  private val changeDashboardMemberRoleMutation =
    """mutation {
      |  changeDashboardMemberRole(
      |    dashboardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    userId: "22222222-2222-2222-2222-222222222222"
      |    roleId: 3
      |  ) {
      |    userId
      |    role {
      |      id
      |      name
      |    }
      |  }
      |}""".stripMargin

  private val duplicateInviteDashboardMemberMutation =
    """mutation {
      |  inviteDashboardMember(
      |    dashboardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    userId: "22222222-2222-2222-2222-222222222222"
      |    roleId: 3
      |  ) {
      |    dashboardId
      |    userId
      |    role {
      |      id
      |      name
      |    }
      |  }
      |}""".stripMargin

  private val removeDashboardMemberMutation =
    """mutation {
      |  removeDashboardMember(
      |    dashboardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    userId: "22222222-2222-2222-2222-222222222222"
      |  )
      |}""".stripMargin

  private val dashboardsAndMembersQuery =
    """query {
      |  myBoards {
      |    id
      |    name
      |    active
      |  }
      |  dashboardMembers(dashboardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") {
      |    dashboardId
      |    userId
      |    role {
      |      id
      |      name
      |    }
      |  }
      |}""".stripMargin

  private val nestedBoardDataQuery =
    """query {
      |  myBoards {
      |    id
      |    owner {
      |      id
      |      firstName
      |      lastName
      |      avatarUrl
      |    }
      |    createdBy {
      |      id
      |      firstName
      |      lastName
      |      avatarUrl
      |    }
      |    lastModifiedBy {
      |      id
      |      firstName
      |      lastName
      |      avatarUrl
      |    }
      |    membersCount
      |  }
      |  dashboardMembers(dashboardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") {
      |    boardId
      |    dashboardId
      |    role {
      |      id
      |      name
      |      permissions {
      |        id
      |        area
      |        canRead
      |      }
      |    }
      |  }
      |}""".stripMargin
}
