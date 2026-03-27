package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.auth.AuthService
import io.github.oleksiybondar.api.domain.user.User
import io.github.oleksiybondar.api.testkit.support.{InMemoryAuthRepo, InMemoryUserRepo}

object AuthServiceFixtures {

  final case class AuthServiceContext(
    userRepo: InMemoryUserRepo[IO],
    authRepo: InMemoryAuthRepo[IO],
    authService: AuthService[IO]
  )

  def withAuthService[A](
    users: List[User] = List(UserFixtures.sampleUser)
  )(run: AuthServiceContext => IO[A]): A =
    (
      for {
        userRepo <- InMemoryUserRepo.create[IO](users)
        authRepo <- InMemoryAuthRepo.create[IO]()
        authService = io.github.oleksiybondar.api.domain.auth.AuthServiceLive[IO](
          userRepo,
          authRepo
        )
        result <- run(AuthServiceContext(userRepo, authRepo, authService))
      } yield result
    ).unsafeRunSync()
}
