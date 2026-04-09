package io.github.oleksiybondar.api.http.routes.graphql.user

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.github.oleksiybondar.api.domain.user.{Email, UserId, Username}
import io.github.oleksiybondar.api.testkit.fixtures.{GraphQLFixtures, UserFixtures}
import io.github.oleksiybondar.api.testkit.support.GraphQLRequestSupport.graphqlRequest
import munit.FunSuite
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec._

class UserGraphQLRoutesSpec extends FunSuite {

  import GraphQLFixtures.withGraphQLRoutes

  test("POST /graphql returns the requested user for a valid user query") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        userQuery(UserFixtures.sampleUser.id.value.toString),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("user")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("id").toOption, Some(UserFixtures.sampleUser.id.value.toString))
    assertEquals(cursor.get[Option[String]]("username").toOption.flatten, Some("alice"))
    assertEquals(cursor.get[Option[String]]("email").toOption.flatten, Some("alice@example.com"))
    assertEquals(cursor.get[String]("firstName").toOption, Some("Alice"))
    assertEquals(cursor.get[String]("lastName").toOption, Some("Example"))
    assert(!body.noSpaces.contains("passwordHash"))
  }

  test("POST /graphql returns a paginated list of users") {
    val secondUser =
      UserFixtures.user(
        id = io.github.oleksiybondar.api.domain.user.UserId(
          java.util.UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        ),
        username = Some(io.github.oleksiybondar.api.domain.user.Username("bob")),
        email = Some(io.github.oleksiybondar.api.domain.user.Email("bob@example.com"))
      )
    val thirdUser  =
      UserFixtures.user(
        id = io.github.oleksiybondar.api.domain.user.UserId(
          java.util.UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        ),
        username = Some(io.github.oleksiybondar.api.domain.user.Username("charlie")),
        email = Some(io.github.oleksiybondar.api.domain.user.Email("charlie@example.com"))
      )

    val response = withGraphQLRoutes(List(UserFixtures.sampleUser, secondUser, thirdUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        usersQuery(offset = 1, limit = 1),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body  = response.as[Json].unsafeRunSync()
    val users =
      body.hcursor
        .downField("data")
        .downField("users")
        .focus
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assertEquals(users.size, 1)
    assertEquals(
      users.head.hcursor.get[String]("username").toOption,
      Some("bob")
    )
    assert(!body.noSpaces.contains("passwordHash"))
  }

  test("POST /graphql clamps negative pagination arguments to zero") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        usersQuery(offset = -1, limit = -1),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body  = response.as[Json].unsafeRunSync()
    val users =
      body.hcursor
        .downField("data")
        .downField("users")
        .focus
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assertEquals(users.size, 0)
  }

  test("POST /graphql returns null when the requested user does not exist") {
    val response = withGraphQLRoutes(Nil) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        userQuery(UserFixtures.sampleUser.id.value.toString),
                        accessToken = Some(token)
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
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        userQuery("not-a-uuid"),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val errors =
      body.hcursor
        .downField("errors")
        .focus
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)

    assertEquals(response.status, Status.Ok)
    assert(errors.nonEmpty)
  }

  test("POST /graphql updates the current user's profile") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        updateProfileMutation("Alicia", "Updated"),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body   = response.as[Json].unsafeRunSync()
    val cursor = body.hcursor.downField("data").downField("updateProfile")

    assertEquals(response.status, Status.Ok)
    assertEquals(cursor.get[String]("firstName").toOption, Some("Alicia"))
    assertEquals(cursor.get[String]("lastName").toOption, Some("Updated"))
  }

  test("POST /graphql changes the current user's avatar and supports removal") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token          <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        setResponse    <- ctx.httpApp.run(
                            graphqlRequest(
                              changeAvatarMutation(Some("https://cdn.example.com/avatar.png")),
                              accessToken = Some(token)
                            )
                          )
        _              <- setResponse.as[Json]
        removeResponse <- ctx.httpApp.run(
                            graphqlRequest(
                              changeAvatarMutation(None),
                              accessToken = Some(token)
                            )
                          )
      } yield removeResponse
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(
      body.hcursor.downField(
        "data"
      ).downField("changeAvatar").get[Option[String]]("avatarUrl").toOption.flatten,
      None
    )
  }

  test("POST /graphql changes the current user's username") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeUsernameMutation("alice-updated"),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(
      body.hcursor.downField(
        "data"
      ).downField("changeUsername").get[Option[String]]("username").toOption.flatten,
      Some("alice-updated")
    )
  }

  test("POST /graphql returns an error when changing to a blank username") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeUsernameMutation("   "),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Username is required"))
  }

  test("POST /graphql returns an error when changing to an already used username") {
    val otherUser = UserFixtures.user(
      id = io.github.oleksiybondar.api.domain.user.UserId(
        java.util.UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
      ),
      username = Some(io.github.oleksiybondar.api.domain.user.Username("taken-name")),
      email = Some(io.github.oleksiybondar.api.domain.user.Email("other@example.com"))
    )

    val response = withGraphQLRoutes(List(UserFixtures.sampleUser, otherUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeUsernameMutation("taken-name"),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(
      body.noSpaces.contains("Username is already in use")
    )
  }

  test("POST /graphql changes the current user's email") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeEmailMutation("alice+updated@example.com"),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(
      body.hcursor.downField(
        "data"
      ).downField("changeEmail").get[Option[String]]("email").toOption.flatten,
      Some("alice+updated@example.com")
    )
  }

  test("POST /graphql returns an error when changing to an already used email") {
    val otherUser = UserFixtures.user(
      id = UserId(java.util.UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
      username = Some(Username("other")),
      email = Some(Email("taken@example.com"))
    )

    val response = withGraphQLRoutes(List(UserFixtures.sampleUser, otherUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeEmailMutation("taken@example.com"),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Email is already in use"))
  }

  test("POST /graphql returns an error when changing to a blank email") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeEmailMutation("   "),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Email is required"))
  }

  test("POST /graphql returns an error when changing to an invalid email") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changeEmailMutation("not-an-email"),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Email is invalid"))
  }

  test("POST /graphql changes the current user's password when the current password is valid") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changePasswordMutation("secret", "secret123"),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assertEquals(
      body.hcursor.downField("data").downField("changePassword").as[Boolean].toOption,
      Some(true)
    )
  }

  test("POST /graphql returns an error when the current password is invalid") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changePasswordMutation("wrong-password", "secret123"),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(
      body.noSpaces.contains("Current password is invalid")
    )
  }

  test("POST /graphql returns an error when the new password is weak") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        changePasswordMutation("secret", "short"),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Password does not satisfy the strength requirements"))
  }

  test("POST /graphql returns an error when the new password was already used") {
    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token          <- ctx.issueAccessToken(UserFixtures.sampleUser.id)
        firstResponse  <- ctx.httpApp.run(
                            graphqlRequest(
                              changePasswordMutation("secret", "secret123"),
                              accessToken = Some(token)
                            )
                          )
        _              <- firstResponse.as[Json]
        secondResponse <- ctx.httpApp.run(
                            graphqlRequest(
                              changePasswordMutation("secret123", "secret123"),
                              accessToken = Some(token)
                            )
                          )
      } yield secondResponse
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("New password must not reuse a previous password"))
  }

  test("POST /graphql returns an error when the authenticated user no longer exists") {
    val missingUserId = UserId(java.util.UUID.fromString("99999999-9999-9999-9999-999999999999"))

    val response = withGraphQLRoutes(List(UserFixtures.sampleUser)) { ctx =>
      for {
        token    <- ctx.issueAccessToken(missingUserId)
        response <- ctx.httpApp.run(
                      graphqlRequest(
                        updateProfileMutation("Ghost", "User"),
                        accessToken = Some(token)
                      )
                    )
      } yield response
    }

    val body = response.as[Json].unsafeRunSync()

    assertEquals(response.status, Status.Ok)
    assert(body.noSpaces.contains("Current user was not found"))
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

  private def usersQuery(offset: Int, limit: Int): String =
    s"""query {
       |  users(offset: $offset, limit: $limit) {
       |    id
       |    username
       |    email
       |    firstName
       |    lastName
       |    avatarUrl
       |    createdAt
       |  }
       |}""".stripMargin

  private def updateProfileMutation(firstName: String, lastName: String): String =
    s"""mutation {
       |  updateProfile(firstName: "$firstName", lastName: "$lastName") {
       |    id
       |    firstName
       |    lastName
       |  }
       |}""".stripMargin

  private def changeAvatarMutation(avatarUrl: Option[String]): String =
    avatarUrl match {
      case Some(value) =>
        s"""mutation {
           |  changeAvatar(avatarUrl: "$value") {
           |    id
           |    avatarUrl
           |  }
           |}""".stripMargin
      case None        =>
        """mutation {
          |  changeAvatar(avatarUrl: null) {
          |    id
          |    avatarUrl
          |  }
          |}""".stripMargin
    }

  private def changeUsernameMutation(username: String): String =
    s"""mutation {
       |  changeUsername(username: "$username") {
       |    id
       |    username
       |  }
       |}""".stripMargin

  private def changeEmailMutation(email: String): String =
    s"""mutation {
       |  changeEmail(email: "$email") {
       |    id
       |    email
       |  }
       |}""".stripMargin

  private def changePasswordMutation(currentPassword: String, newPassword: String): String =
    s"""mutation {
       |  changePassword(currentPassword: "$currentPassword", newPassword: "$newPassword")
       |}""".stripMargin
}
