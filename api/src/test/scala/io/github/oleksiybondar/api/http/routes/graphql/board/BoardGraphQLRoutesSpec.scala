package io.github.oleksiybondar.api.http.routes.graphql.board

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.github.oleksiybondar.api.domain.board.{BoardId, BoardName}
import io.github.oleksiybondar.api.domain.user.{
  Email,
  FirstName,
  LastName,
  PasswordHash,
  UserId,
  Username
}
import io.github.oleksiybondar.api.testkit.fixtures.{
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

  test("POST /graphql filters boards by keyword and owner") {
    val otherOwnerId       = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
    val matchingDashboard  =
      io.github.oleksiybondar.api.testkit.fixtures.BoardFixtures.sampleDashboard.copy(
        name = BoardName("Platform Board")
      )
    val filteredOutByName  =
      io.github.oleksiybondar.api.testkit.fixtures.BoardFixtures.dashboard(
        id = BoardId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")),
        name = BoardName("Infrastructure Board")
      )
    val filteredOutByOwner =
      io.github.oleksiybondar.api.testkit.fixtures.BoardFixtures.dashboard(
        id = BoardId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")),
        name = BoardName("Platform Ops"),
        ownerUserId = otherOwnerId,
        createdByUserId = otherOwnerId,
        lastModifiedByUserId = otherOwnerId
      )
    val memberships        = List(
      io.github.oleksiybondar.api.testkit.fixtures.BoardMemberFixtures.sampleMember.copy(
        boardId = matchingDashboard.id
      ),
      io.github.oleksiybondar.api.testkit.fixtures.BoardMemberFixtures.sampleMember.copy(
        boardId = filteredOutByName.id
      ),
      io.github.oleksiybondar.api.testkit.fixtures.BoardMemberFixtures.sampleMember.copy(
        boardId = filteredOutByOwner.id
      )
    )

    val response = withGraphQLRoutes(
      users = List(UserFixtures.sampleUser),
      dashboards = List(matchingDashboard, filteredOutByName, filteredOutByOwner),
      members = memberships
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        filteredBoardsQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body       = response.as[Json].unsafeRunSync()
    val dashboards =
      body.hcursor.downField(
        "data"
      ).downField("myBoards").focus.flatMap(_.asArray).getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assertEquals(dashboards.size, 1)
    assertEquals(dashboards.head.hcursor.get[String]("name").toOption, Some("Platform Board"))
  }

  test("POST /graphql can include inactive boards when active filter is null") {
    val inactiveBoard =
      io.github.oleksiybondar.api.testkit.fixtures.BoardFixtures.dashboard(
        id = BoardId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")),
        name = BoardName("Archived Board"),
        active = false
      )
    val memberships   = List(
      io.github.oleksiybondar.api.testkit.fixtures.BoardMemberFixtures.sampleMember.copy(
        boardId = io.github.oleksiybondar.api.testkit.fixtures.BoardFixtures.sampleDashboard.id
      ),
      io.github.oleksiybondar.api.testkit.fixtures.BoardMemberFixtures.sampleMember.copy(
        boardId = inactiveBoard.id
      )
    )

    val response = withGraphQLRoutes(
      users = List(UserFixtures.sampleUser),
      dashboards = List(
        io.github.oleksiybondar.api.testkit.fixtures.BoardFixtures.sampleDashboard,
        inactiveBoard
      ),
      members = memberships
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        boardsIncludingInactiveQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body       = response.as[Json].unsafeRunSync()
    val dashboards =
      body.hcursor.downField(
        "data"
      ).downField("myBoards").focus.flatMap(_.asArray).getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assertEquals(dashboards.size, 2)
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

  test("POST /graphql returns a single board for the current user") {
    val response = withGraphQLRoutes(
      users = List(UserFixtures.sampleUser),
      tickets = List(TicketFixtures.sampleTicket),
      timeEntries = List(TimeTrackingFixtures.sampleEntry)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        boardQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("board")

    assertEquals(response.status, Status.Ok)
    assertEquals(
      cursor.get[String]("id").toOption,
      Some("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    )
    assertEquals(cursor.get[String]("name").toOption, Some("Core Board"))
    assertEquals(cursor.downField("owner").get[String]("firstName").toOption, Some("Alice"))
    assertEquals(
      cursor.downField("createdBy").get[String]("lastName").toOption,
      Some("Example")
    )
    assertEquals(
      cursor.downField("currentUserRole").get[String]("name").toOption,
      Some("admin")
    )
    assertEquals(
      cursor.downField(
        "currentUserRole"
      ).downField("permissions").downArray.get[String]("area").toOption,
      Some("dashboard")
    )
    assertEquals(
      cursor.downField("tickets").downArray.get[String]("status").toOption,
      Some("new")
    )
    assertEquals(
      cursor.downField("tickets").downArray.get[Int]("trackedMinutes").toOption,
      Some(90)
    )
  }

  test("POST /graphql still supports the legacy dashboard field alias") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        dashboardLegacyQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("dashboard")

    assertEquals(response.status, Status.Ok)
    assertEquals(
      cursor.get[String]("id").toOption,
      Some("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    )
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
      members.head.hcursor.downField("user").get[String]("firstName").toOption,
      Some("Alice")
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

  test("POST /graphql changes a board title") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeBoardTitleMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("changeBoardTitle")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("name").toOption, Some("Platform Board"))
  }

  test("POST /graphql changes a board description") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeBoardDescriptionMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("changeBoardDescription")

    assertEquals(response.status, Status.Ok)
    assertEquals(
      cursor.get[Option[String]]("description").toOption.flatten,
      Some("Updated board description")
    )
  }

  test("POST /graphql changes board ownership by username") {
    val newOwner =
      UserFixtures.user(
        id = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        username = Some(Username("bob")),
        email = Some(Email("bob@example.com")),
        passwordHash = PasswordHash("hash:bob"),
        firstName = FirstName("Bob"),
        lastName = LastName("Owner")
      )

    val response = withGraphQLRoutes(users = List(UserFixtures.sampleUser, newOwner)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeBoardOwnershipMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("changeBoardOwnership")

    assertEquals(response.status, Status.Ok)
    assertEquals(
      cursor.get[String]("ownerUserId").toOption,
      Some("22222222-2222-2222-2222-222222222222")
    )
  }

  test("POST /graphql deactivates a board") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        deactivateBoardMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("deactivateBoard")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[Boolean]("active").toOption, Some(false))
  }

  test("POST /graphql activates a board") {
    val inactiveBoard =
      io.github.oleksiybondar.api.testkit.fixtures.BoardFixtures.sampleDashboard.copy(
        active = false
      )

    val response = withGraphQLRoutes(
      users = List(UserFixtures.sampleUser),
      dashboards = List(inactiveBoard)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        activateBoardMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("activateBoard")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[Boolean]("active").toOption, Some(true))
  }

  test("POST /graphql invites a dashboard member") {
    val invitedUser =
      UserFixtures.user(
        id = UserId(UUID.fromString("44444444-4444-4444-4444-444444444444")),
        username = Some(Username("diana")),
        email = Some(Email("diana@example.com")),
        passwordHash = PasswordHash("hash:diana"),
        firstName = FirstName("Diana"),
        lastName = LastName("Member")
      )

    val response = withGraphQLRoutes(List(UserFixtures.sampleUser, invitedUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        inviteBoardMemberMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("inviteBoardMember")

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

  test("POST /graphql changes a board member role") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeBoardMemberRoleMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("changeBoardMemberRole")

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

  test("POST /graphql removes a board member") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        removeBoardMemberMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(
      body.hcursor.downField("data").get[Boolean]("removeBoardMember").toOption,
      Some(true)
    )
  }

  test("POST /graphql returns an error when removing yourself from a board") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        removeCurrentBoardMemberMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("You cannot remove yourself from the board"))
  }

  test("POST /graphql returns an error when removing the last board member") {
    val onlyMember =
      io.github.oleksiybondar.api.testkit.fixtures.BoardMemberFixtures.member(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
      )

    val response = withGraphQLRoutes(
      users = List(UserFixtures.sampleUser),
      members = List(onlyMember)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        removeCurrentBoardMemberMutationAsAdminForSingleMemberBoard,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("The last board member cannot be removed"))
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

  test("POST /graphql returns an error when board id is missing") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        missingBoardIdQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Board id is required"))
  }

  test("POST /graphql returns an error when board id is not a UUID") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        invalidBoardIdQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Invalid UUID: not-a-uuid"))
  }

  test("POST /graphql returns an error when changing ownership to an unknown user login") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        missingOwnerMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Owner user was not found"))
  }

  test("POST /graphql returns an error when inviting an unknown user login") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        missingInvitedUserMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Invited user was not found"))
  }

  test("POST /graphql returns an error when inviting a member without user input") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        missingMemberInputMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("User is required"))
  }

  test("POST /graphql denies board title changes for a read-only member") {
    val viewerMembership =
      io.github.oleksiybondar.api.testkit.fixtures.BoardMemberFixtures.sampleMember.copy(
        roleId = io.github.oleksiybondar.api.domain.permission.RoleId(3)
      )

    val response = withGraphQLRoutes(
      users = List(UserFixtures.sampleUser),
      members = List(viewerMembership)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeBoardTitleMutation,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Board title could not be changed"))
  }

  test("POST /graphql allows read access to an inactive board for members") {
    val inactiveBoard =
      io.github.oleksiybondar.api.testkit.fixtures.BoardFixtures.sampleDashboard.copy(active =
        false
      )
    val viewerMember  =
      io.github.oleksiybondar.api.testkit.fixtures.BoardMemberFixtures.sampleMember.copy(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = io.github.oleksiybondar.api.domain.permission.RoleId(3)
      )

    val response = withGraphQLRoutes(
      users = List(
        UserFixtures.sampleUser,
        UserFixtures.user(
          id = viewerMember.userId,
          username = Some(Username("viewer")),
          email = Some(Email("viewer@example.com")),
          passwordHash = PasswordHash("hash:viewer"),
          firstName = FirstName("Read"),
          lastName = LastName("Only")
        )
      ),
      dashboards = List(inactiveBoard),
      members = List(viewerMember)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(viewerMember.userId)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        boardQuery,
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    val boardId =
      body.hcursor
        .downField("data")
        .downField("board")
        .get[String]("id")
        .toOption
    val errors  = body.hcursor.downField("errors").focus

    assertEquals(boardId, Some(inactiveBoard.id.value.toString))
    assertEquals(errors, None)
  }

  test("POST /graphql allows reading inactive board members for members") {
    val inactiveBoard =
      io.github.oleksiybondar.api.testkit.fixtures.BoardFixtures.sampleDashboard.copy(active =
        false
      )
    val viewerMember  =
      io.github.oleksiybondar.api.testkit.fixtures.BoardMemberFixtures.sampleMember.copy(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = io.github.oleksiybondar.api.domain.permission.RoleId(3)
      )

    val response = withGraphQLRoutes(
      users = List(
        UserFixtures.sampleUser,
        UserFixtures.user(
          id = viewerMember.userId,
          username = Some(Username("viewer")),
          email = Some(Email("viewer@example.com")),
          passwordHash = PasswordHash("hash:viewer"),
          firstName = FirstName("Read"),
          lastName = LastName("Only")
        )
      ),
      dashboards = List(inactiveBoard),
      members = List(viewerMember)
    ) { ctx =>
      for {
        token    <- ctx.issueAccessToken(viewerMember.userId)
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
    val errors  = body.hcursor.downField("errors").focus

    assertEquals(response.status, Status.Ok)
    assertEquals(members.nonEmpty, true)
    assertEquals(errors, None)
  }

  private val dashboardMembersQuery =
    """query {
      |  dashboardMembers(dashboardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") {
      |    dashboardId
      |    userId
      |    createdAt
      |    user {
      |      id
      |      firstName
      |      lastName
      |      avatarUrl
      |    }
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

  private val boardQuery =
    """query {
      |  board(boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") {
      |    id
      |    name
      |    active
      |    owner { firstName avatarUrl }
      |    createdBy { lastName }
      |    currentUserRole {
      |      name
      |      permissions { area }
      |    }
      |    tickets {
      |      name
      |      status
      |      estimatedMinutes
      |      trackedMinutes
      |    }
      |  }
      |}""".stripMargin

  private val dashboardLegacyQuery =
    """query {
      |  dashboard(dashboardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") {
      |    id
      |    name
      |    active
      |  }
      |}""".stripMargin

  private val missingBoardIdQuery =
    """query {
      |  board {
      |    id
      |  }
      |}""".stripMargin

  private val invalidBoardIdQuery =
    """query {
      |  board(boardId: "not-a-uuid") {
      |    id
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

  private val filteredBoardsQuery =
    """query {
      |  myBoards(
      |    keyword: "platform"
      |    ownerUserId: "11111111-1111-1111-1111-111111111111"
      |  ) {
      |    id
      |    name
      |    active
      |  }
      |}""".stripMargin

  private val boardsIncludingInactiveQuery =
    """query {
      |  myBoards(active: null) {
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

  private val changeBoardTitleMutation =
    """mutation {
      |  changeBoardTitle(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    name: "Platform Board"
      |  ) {
      |    id
      |    name
      |  }
      |}""".stripMargin

  private val changeBoardDescriptionMutation =
    """mutation {
      |  changeBoardDescription(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    description: "Updated board description"
      |  ) {
      |    id
      |    description
      |  }
      |}""".stripMargin

  private val changeBoardOwnershipMutation =
    """mutation {
      |  changeBoardOwnership(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    owner: "bob"
      |  ) {
      |    id
      |    ownerUserId
      |  }
      |}""".stripMargin

  private val missingOwnerMutation =
    """mutation {
      |  changeBoardOwnership(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    owner: "missing-user"
      |  ) {
      |    id
      |    ownerUserId
      |  }
      |}""".stripMargin

  private val deactivateBoardMutation =
    """mutation {
      |  deactivateBoard(boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") {
      |    id
      |    active
      |  }
      |}""".stripMargin

  private val activateBoardMutation =
    """mutation {
      |  activateBoard(boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa") {
      |    id
      |    active
      |  }
      |}""".stripMargin

  private val inviteBoardMemberMutation =
    """mutation {
      |  inviteBoardMember(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    user: "diana"
      |    roleId: 3
      |  ) {
      |    boardId
      |    userId
      |    role {
      |      id
      |      name
      |    }
      |  }
      |}""".stripMargin

  private val missingInvitedUserMutation =
    """mutation {
      |  inviteBoardMember(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    user: "unknown-user"
      |    roleId: 3
      |  ) {
      |    boardId
      |    userId
      |  }
      |}""".stripMargin

  private val missingMemberInputMutation =
    """mutation {
      |  inviteBoardMember(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    roleId: 3
      |  ) {
      |    boardId
      |    userId
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

  private val changeBoardMemberRoleMutation =
    """mutation {
      |  changeBoardMemberRole(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
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

  private val removeBoardMemberMutation =
    """mutation {
      |  removeBoardMember(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    userId: "22222222-2222-2222-2222-222222222222"
      |  )
      |}""".stripMargin

  private val removeCurrentBoardMemberMutation =
    """mutation {
      |  removeBoardMember(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
      |    userId: "11111111-1111-1111-1111-111111111111"
      |  )
      |}""".stripMargin

  private val removeCurrentBoardMemberMutationAsAdminForSingleMemberBoard =
    """mutation {
      |  removeBoardMember(
      |    boardId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
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
      |    user {
      |      id
      |      firstName
      |      lastName
      |    }
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
