package io.github.oleksiybondar.api.domain.auth

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref}
import io.github.oleksiybondar.api.domain.auth.password.PasswordHasher
import io.github.oleksiybondar.api.domain.auth.password.PasswordStrengthError.PasswordTooShort
import io.github.oleksiybondar.api.domain.user.{Email, Username}
import io.github.oleksiybondar.api.infrastructure.auth.JwtServiceLive
import io.github.oleksiybondar.api.testkit.fixtures.AuthServiceFixtures.withAuthService
import io.github.oleksiybondar.api.testkit.fixtures.{AuthServiceFixtures, UserFixtures}
import io.github.oleksiybondar.api.testkit.support.{InMemoryAuthRepo, InMemoryUserRepo}
import munit.FunSuite

class AuthServiceLiveSpec extends FunSuite {

  test("register creates tokens for a newly created user") {
    val result = withAuthService(Nil) { ctx =>
      ctx.authService
        .register(
          RegisterUserCommand(
            email = "  Alice@Example.com ",
            password = "secret123",
            firstName = "Alice",
            lastName = "Example",
            username = Some(Username("alice"))
          )
        )
        .value
    }

    val tokens = result.toOption.get
    assertNotEquals(tokens.accessToken.value, "")
    assertNotEquals(tokens.refreshToken.value.toString, "")
  }

  test("register rejects an empty email") {
    val result = withAuthService(Nil) { ctx =>
      ctx.authService
        .register(
          RegisterUserCommand(
            email = "   ",
            password = "secret123",
            firstName = "Alice",
            lastName = "Example",
            username = None
          )
        )
        .value
    }

    assertEquals(result, Left(AuthError.EmailRequired))
  }

  test("register rejects malformed email patterns") {
    val invalidEmails = List(
      "plainaddress",
      "@example.com",
      "alice@",
      "alice@example",
      "alice..dots@example.com",
      "alice@example..com"
    )

    invalidEmails.foreach { invalidEmail =>
      val result = withAuthService(Nil) { ctx =>
        ctx.authService
          .register(
            RegisterUserCommand(
              email = invalidEmail,
              password = "secret123",
              firstName = "Alice",
              lastName = "Example",
              username = Some(Username("alice"))
            )
          )
          .value
      }

      assertEquals(result, Left(AuthError.InvalidEmail))
    }
  }

  test("register returns WeakPassword with strength errors for a weak password") {
    val result = withAuthService(Nil) { ctx =>
      ctx.authService
        .register(
          RegisterUserCommand(
            email = "alice@example.com",
            password = "short",
            firstName = "Alice",
            lastName = "Example",
            username = Some(Username("alice"))
          )
        )
        .value
    }

    result match {
      case Left(AuthError.WeakPassword(errors)) => assertEquals(errors, List(PasswordTooShort(8)))
      case other                                => fail(s"Expected WeakPassword, got: $other")
    }
  }

  test("register rejects a duplicate email") {
    val result = withAuthService(List(UserFixtures.sampleUser)) { ctx =>
      ctx.authService
        .register(
          RegisterUserCommand(
            email = "alice@example.com",
            password = "secret123",
            firstName = "Alice",
            lastName = "Example",
            username = None
          )
        )
        .value
    }

    assertEquals(result, Left(AuthError.EmailAlreadyUsed))
  }

  test("register fails fast on duplicate email before hashing the password") {
    val result = (
      for {
        hashCalls  <- Ref.of[IO, Int](0)
        userRepo   <- InMemoryUserRepo.create[IO](List(UserFixtures.sampleUser))
        authRepo   <- InMemoryAuthRepo.create[IO]()
        authService = new AuthServiceLive[IO](
                        userRepo,
                        authRepo,
                        new JwtServiceLive[IO](AuthServiceFixtures.testAuthConfig.jwtSecret),
                        new PasswordHasher[IO] {
                          override def hash(password: String)
                              : IO[io.github.oleksiybondar.api.domain.user.PasswordHash] =
                            hashCalls.update(_ + 1) *> IO.pure(
                              io.github.oleksiybondar.api.domain.user.PasswordHash(
                                s"hash:$password"
                              )
                            )

                          override def verify(
                              password: String,
                              hash: io.github.oleksiybondar.api.domain.user.PasswordHash
                          ): IO[Boolean] =
                            IO.pure(hash == io.github.oleksiybondar.api.domain.user.PasswordHash(
                              s"hash:$password"
                            ))
                        },
                        AuthServiceFixtures.passwordStrengthValidator,
                        AuthServiceFixtures.unsafeEmptyPasswordHistory,
                        accessTokenTtlSeconds =
                          AuthServiceFixtures.testAuthConfig.accessTokenTtlSeconds,
                        sessionTtlDays = AuthServiceFixtures.testAuthConfig.sessionTtlDays
                      )
        outcome    <- authService
                        .register(
                          RegisterUserCommand(
                            email = "alice@example.com",
                            password = "secret123",
                            firstName = "Alice",
                            lastName = "Example",
                            username = None
                          )
                        )
                        .value
        hashCount  <- hashCalls.get
      } yield (outcome, hashCount)
    ).unsafeRunSync()

    assertEquals(result._1, Left(AuthError.EmailAlreadyUsed))
    assertEquals(result._2, 0)
  }

  test("register records the initial password in password history") {
    val result = withAuthService(Nil) { ctx =>
      for {
        _       <- ctx.authService
                     .register(
                       RegisterUserCommand(
                         email = "alice@example.com",
                         password = "secret123",
                         firstName = "Alice",
                         lastName = "Example",
                         username = None
                       )
                     )
                     .value
        user    <- ctx.userRepo.findByEmail(Email("alice@example.com"))
        wasUsed <- ctx.passwordHistory.wasUsedBefore(user.get.id, "secret123")
      } yield wasUsed
    }

    assertEquals(result, true)
  }

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

  test("login returns invalid credentials when the password does not match") {
    val result = withAuthService(List(UserFixtures.sampleUser)) { ctx =>
      ctx.authService.login("alice", "wrong-password").value
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
