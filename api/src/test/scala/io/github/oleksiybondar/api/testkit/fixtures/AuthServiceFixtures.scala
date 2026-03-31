package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref}
import io.github.oleksiybondar.api.config.{AuthConfig, PasswordStrengthConfig}
import io.github.oleksiybondar.api.domain.auth.password.{PasswordHasher, PasswordHistory}
import io.github.oleksiybondar.api.domain.auth.{AuthService, AuthServiceLive, JwtService}
import io.github.oleksiybondar.api.domain.user.{PasswordHash, User, UserId}
import io.github.oleksiybondar.api.infrastructure.auth.JwtServiceLive
import io.github.oleksiybondar.api.infrastructure.auth.password.PasswordStrengthValidatorLive
import io.github.oleksiybondar.api.testkit.support.{InMemoryAuthRepo, InMemoryUserRepo}

object AuthServiceFixtures {

  final case class AuthServiceContext(
      userRepo: InMemoryUserRepo[IO],
      authRepo: InMemoryAuthRepo[IO],
      jwtService: JwtService[IO],
      passwordHistory: PasswordHistory[IO],
      authService: AuthService[IO]
  )

  val testAuthConfig: AuthConfig =
    AuthConfig(
      jwtSecret = "test-jwt-secret",
      accessTokenTtlSeconds = 900,
      sessionTtlDays = 30
    )

  val passwordStrengthValidator: PasswordStrengthValidatorLive =
    new PasswordStrengthValidatorLive(
      PasswordStrengthConfig(
        minLength = 8,
        requireDigit = false,
        requireSpecialChar = false
      )
    )

  def withAuthService[A](
      users: List[User] = List(UserFixtures.sampleUser)
  )(run: AuthServiceContext => IO[A]): A =
    (
      for {
        userRepo        <- InMemoryUserRepo.create[IO](users)
        authRepo        <- InMemoryAuthRepo.create[IO]()
        passwordHistory <- InMemoryPasswordHistory.create
        jwtService       = new JwtServiceLive[IO](testAuthConfig.jwtSecret)
        authService      = new AuthServiceLive[IO](
                             userRepo,
                             authRepo,
                             jwtService,
                             fakePasswordHasher,
                             passwordStrengthValidator,
                             passwordHistory,
                             accessTokenTtlSeconds = testAuthConfig.accessTokenTtlSeconds,
                             sessionTtlDays = testAuthConfig.sessionTtlDays
                           )
        result          <- run(
                             AuthServiceContext(
                               userRepo,
                               authRepo,
                               jwtService,
                               passwordHistory,
                               authService
                             )
                           )
      } yield result
    ).unsafeRunSync()

  object fakePasswordHasher extends PasswordHasher[IO] {
    override def hash(password: String): IO[PasswordHash] =
      IO.pure(PasswordHash(s"hash:$password"))

    override def verify(password: String, hash: PasswordHash): IO[Boolean] =
      IO.pure(hash == PasswordHash(s"hash:$password"))
  }

  private final class InMemoryPasswordHistory private (
      state: Ref[IO, Map[UserId, List[PasswordHash]]]
  ) extends PasswordHistory[IO] {

    override def record(userId: UserId, hash: PasswordHash): IO[Unit] =
      state.update(current => current.updated(userId, hash :: current.getOrElse(userId, Nil)))

    override def wasUsedBefore(userId: UserId, password: String): IO[Boolean] =
      state.get.map(
        _.getOrElse(userId, Nil).contains(PasswordHash(s"hash:$password"))
      )

    override def clear(userId: UserId): IO[Unit] =
      state.update(_ - userId)
  }

  private object InMemoryPasswordHistory {
    def create: IO[InMemoryPasswordHistory] =
      Ref
        .of[IO, Map[UserId, List[PasswordHash]]](Map.empty)
        .map(new InMemoryPasswordHistory(_))
  }

  def unsafeEmptyPasswordHistory: PasswordHistory[IO] =
    InMemoryPasswordHistory.create.unsafeRunSync()
}
