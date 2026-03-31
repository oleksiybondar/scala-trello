package io.github.oleksiybondar.api.domain.auth

import io.github.oleksiybondar.api.testkit.fixtures.AuthServiceFixtures.withAuthService
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import munit.FunSuite

class AuthServiceLiveSpec extends FunSuite {

  test("login returns tokens when a user exists for the given username") {
    val user = UserFixtures.sampleUser

    val result = withAuthService(List(user)) { ctx =>
      ctx.authService.login("alice", "secret").value
    }

    assert(result.isRight)
    assertNotEquals(result.toOption.get.accessToken.value, "")
    assertNotEquals(result.toOption.get.refreshToken.value.toString, "")
  }

  test("login returns invalid credentials when a user does not exist") {
    val result = withAuthService(Nil) { ctx =>
      ctx.authService.login("missing-user", "secret").value
    }

    assertEquals(result, Left(AuthError.InvalidCredentials))
  }

  test("refresh returns new tokens for a valid refresh token") {
    val user = UserFixtures.sampleUser

    val result = withAuthService(List(user)) { ctx =>
      for {
        loginTokens     <- ctx.authService.login("alice@example.com", "secret").value
        refreshedTokens <- ctx.authService.refresh(loginTokens.toOption.get.refreshToken).value
      } yield (loginTokens, refreshedTokens)
    }

    val (loginTokens, refreshedTokens) = result
    assert(loginTokens.isRight)
    assert(refreshedTokens.isRight)
    assertNotEquals(
      refreshedTokens.toOption.get.refreshToken,
      loginTokens.toOption.get.refreshToken
    )
    assertNotEquals(refreshedTokens.toOption.get.accessToken, loginTokens.toOption.get.accessToken)
  }

  test("refresh token cannot be reused after rotation") {
    val user = UserFixtures.sampleUser

    val result = withAuthService(List(user)) { ctx =>
      for {
        loginTokens    <- ctx.authService.login("alice@example.com", "secret").value
        _              <- ctx.authService.refresh(loginTokens.toOption.get.refreshToken).value
        reusedOldToken <- ctx.authService.refresh(loginTokens.toOption.get.refreshToken).value
      } yield reusedOldToken
    }

    assertEquals(result, Left(AuthError.InvalidRefreshToken))
  }

  test("verifyToken returns the matching user id for a valid access token") {
    val user = UserFixtures.sampleUser

    val result = withAuthService(List(user)) { ctx =>
      for {
        loginTokens <- ctx.authService.login("alice@example.com", "secret").value
        userId      <- ctx.authService.verifyToken(loginTokens.toOption.get.accessToken)
      } yield userId
    }

    assertEquals(result, Some(user.id))
  }

  test("verifyToken returns none for an invalid access token") {
    val result = withAuthService() { ctx =>
      ctx.authService.verifyToken(AccessToken("missing-token"))
    }

    assertEquals(result, None)
  }

  test("logout revokes the session and makes refresh fail") {
    val user = UserFixtures.sampleUser

    val result = withAuthService(List(user)) { ctx =>
      for {
        loginTokens <- ctx.authService.login("alice@example.com", "secret").value
        _           <- ctx.authService.logout(loginTokens.toOption.get.refreshToken)
        refreshed   <- ctx.authService.refresh(loginTokens.toOption.get.refreshToken).value
      } yield refreshed
    }

    assertEquals(result, Left(AuthError.InvalidRefreshToken))
  }
}
