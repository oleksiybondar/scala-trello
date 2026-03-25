package io.github.oleksiybondar.api.domain.auth

import io.github.oleksiybondar.api.domain.user.User
import io.github.oleksiybondar.api.testkit.fixtures.AuthServiceFixtures.withAuthService
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import munit.FunSuite

class AuthServiceLiveSpec extends FunSuite {

  test("login returns tokens when a user exists for the given username") {
    val user = UserFixtures.sampleUser

    val result = withAuthService(List(user)) { ctx =>
      ctx.authService.login(LoginCommand("alice", "secret"))
    }

    assert(result.nonEmpty)
    assertNotEquals(result.get.accessToken.value, "")
    assertNotEquals(result.get.refreshToken.value, "")
  }

  test("login returns none when a user does not exist") {
    val result = withAuthService(Nil) { ctx =>
      ctx.authService.login(LoginCommand("missing-user", "secret"))
    }

    assertEquals(result, None)
  }

  test("refresh returns new tokens for a valid refresh token") {
    val user = UserFixtures.sampleUser

    val result = withAuthService(List(user)) { ctx =>
      for {
        loginTokens <- ctx.authService.login(LoginCommand("alice@example.com", "secret"))
        refreshedTokens <- ctx.authService.refresh(
          RefreshTokenCommand(loginTokens.get.refreshToken)
        )
      } yield (loginTokens, refreshedTokens)
    }

    val (loginTokens, refreshedTokens) = result
    assert(loginTokens.nonEmpty)
    assert(refreshedTokens.nonEmpty)
    assertNotEquals(refreshedTokens.get.refreshToken, loginTokens.get.refreshToken)
    assertNotEquals(refreshedTokens.get.accessToken, loginTokens.get.accessToken)
  }

  test("refresh token cannot be reused after rotation") {
    val user = UserFixtures.sampleUser

    val result = withAuthService(List(user)) { ctx =>
      for {
        loginTokens <- ctx.authService.login(LoginCommand("alice@example.com", "secret"))
        _ <- ctx.authService.refresh(
          RefreshTokenCommand(loginTokens.get.refreshToken)
        )
        reusedOldToken <- ctx.authService.refresh(
          RefreshTokenCommand(loginTokens.get.refreshToken)
        )
      } yield reusedOldToken
    }

    assertEquals(result, None)
  }
}
