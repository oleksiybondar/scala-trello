package io.github.oleksiybondar.api.infrastructure.db.auth

import cats.effect.IO
import io.github.oleksiybondar.api.domain.auth.{AccessToken, RefreshToken}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.infrastructure.db.user.SlickUserRepo
import io.github.oleksiybondar.api.testkit.fixtures.SlickUserRepoFixtures.withCleanDatabase
import io.github.oleksiybondar.api.testkit.fixtures.UserFixtures
import munit.FunSuite

import java.util.UUID
import scala.concurrent.ExecutionContext

class SlickTokenRepoSpec extends FunSuite {

  given ExecutionContext = ExecutionContext.global

  test("saveAccessToken persists a token that can be looked up by access token") {
    withRepos { (userRepo, tokenRepo, user) =>
      for {
        _      <- userRepo.create(user)
        _      <- tokenRepo.saveAccessToken(AccessToken("access-1"), user.id)
        result <- tokenRepo.findUserIdByAccessToken(AccessToken("access-1"))
      } yield assertEquals(result, Some(user.id))
    }
  }

  test("saveRefreshToken persists a token that can be looked up by refresh token") {
    withRepos { (userRepo, tokenRepo, user) =>
      for {
        _      <- userRepo.create(user)
        _      <- tokenRepo.saveRefreshToken(RefreshToken("refresh-1"), user.id)
        result <- tokenRepo.findUserIdByRefreshToken(RefreshToken("refresh-1"))
      } yield assertEquals(result, Some(user.id))
    }
  }

  test("rotateRefreshToken removes the old token and stores the new one") {
    withRepos { (userRepo, tokenRepo, user) =>
      for {
        _              <- userRepo.create(user)
        _              <- tokenRepo.saveRefreshToken(RefreshToken("refresh-old"), user.id)
        _              <- tokenRepo.rotateRefreshToken(
                            RefreshToken("refresh-old"),
                            RefreshToken("refresh-new"),
                            user.id
                          )
        oldTokenResult <- tokenRepo.findUserIdByRefreshToken(RefreshToken("refresh-old"))
        newTokenResult <- tokenRepo.findUserIdByRefreshToken(RefreshToken("refresh-new"))
      } yield {
        assertEquals(oldTokenResult, None)
        assertEquals(newTokenResult, Some(user.id))
      }
    }
  }

  test("deleteRefreshToken removes the refresh token") {
    withRepos { (userRepo, tokenRepo, user) =>
      for {
        _      <- userRepo.create(user)
        _      <- tokenRepo.saveRefreshToken(RefreshToken("refresh-delete"), user.id)
        _      <- tokenRepo.deleteRefreshToken(RefreshToken("refresh-delete"))
        result <- tokenRepo.findUserIdByRefreshToken(RefreshToken("refresh-delete"))
      } yield assertEquals(result, None)
    }
  }

  test("findUserIdByAccessToken returns none when the token does not exist") {
    withRepos { (_, tokenRepo, _) =>
      tokenRepo
        .findUserIdByAccessToken(AccessToken("missing-access-token"))
        .map(result => assertEquals(result, None))
    }
  }

  test("findUserIdByRefreshToken returns none when the token does not exist") {
    withRepos { (_, tokenRepo, _) =>
      tokenRepo
        .findUserIdByRefreshToken(RefreshToken("missing-refresh-token"))
        .map(result => assertEquals(result, None))
    }
  }

  private def withRepos(run: (
      SlickUserRepo[IO],
      SlickTokenRepo[IO],
      io.github.oleksiybondar.api.domain.user.User
  ) => IO[Unit]): Unit =
    withCleanDatabase { db =>
      val user = UserFixtures.user(
        id = UserId(UUID.fromString("99999999-9999-9999-9999-999999999999")),
        username = Some(io.github.oleksiybondar.api.domain.user.Username("token-user")),
        email = Some(io.github.oleksiybondar.api.domain.user.Email("token-user@example.com"))
      )

      run(
        new SlickUserRepo[IO](db),
        new SlickTokenRepo[IO](db),
        user
      )
    }
}
