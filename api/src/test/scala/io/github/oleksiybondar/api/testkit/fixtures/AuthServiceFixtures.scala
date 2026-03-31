package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.config.AuthConfig
import io.github.oleksiybondar.api.domain.auth.{
  AuthService,
  AuthServiceLive,
  JwtService,
  JwtServiceLive
}
import io.github.oleksiybondar.api.domain.user.User
import io.github.oleksiybondar.api.testkit.support.{InMemoryAuthRepo, InMemoryUserRepo}

object AuthServiceFixtures {

  final case class AuthServiceContext(
      userRepo: InMemoryUserRepo[IO],
      authRepo: InMemoryAuthRepo[IO],
      jwtService: JwtService[IO],
      authService: AuthService[IO]
  )

  val testAuthConfig: AuthConfig =
    AuthConfig(
      jwtSecret = "test-jwt-secret",
      accessTokenTtlSeconds = 900,
      sessionTtlDays = 30
    )

  def withAuthService[A](
      users: List[User] = List(UserFixtures.sampleUser)
  )(run: AuthServiceContext => IO[A]): A =
    (
      for {
        userRepo   <- InMemoryUserRepo.create[IO](users)
        authRepo   <- InMemoryAuthRepo.create[IO]()
        jwtService  = new JwtServiceLive[IO](testAuthConfig.jwtSecret)
        authService = new AuthServiceLive[IO](
                        userRepo,
                        authRepo,
                        jwtService,
                        accessTokenTtlSeconds = testAuthConfig.accessTokenTtlSeconds,
                        sessionTtlDays = testAuthConfig.sessionTtlDays
                      )
        result     <- run(AuthServiceContext(userRepo, authRepo, jwtService, authService))
      } yield result
    ).unsafeRunSync()
}
