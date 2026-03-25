package io.github.oleksiybondar.api.testkit.fixtures

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.user.{User, UserServiceLive}
import io.github.oleksiybondar.api.testkit.support.InMemoryUserRepo

object UserServiceFixtures {

  final case class UserServiceContext(
    userRepo: InMemoryUserRepo[IO],
    userService: UserServiceLive[IO]
  )

  def withUserService[A](
    users: List[User] = Nil
  )(run: UserServiceContext => IO[A]): A =
    (
      for {
        userRepo <- InMemoryUserRepo.create[IO](users)
        userService = new UserServiceLive[IO](userRepo)
        result <- run(UserServiceContext(userRepo, userService))
      } yield result
    ).unsafeRunSync()
}
