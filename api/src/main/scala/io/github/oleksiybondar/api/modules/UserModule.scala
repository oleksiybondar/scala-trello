package io.github.oleksiybondar.api.modules

import cats.effect.Async
import io.github.oleksiybondar.api.domain.user.{UserService, UserServiceLive}
import io.github.oleksiybondar.api.infrastructure.db.user.{SlickUserRepo, UserRepo}
import slick.jdbc.PostgresProfile.api.Database

import scala.concurrent.ExecutionContext

final case class UserModule[F[_]](
                                   userRepo: UserRepo[F],
                                   userService: UserService[F],
                                 )

object UserModule {

  def make[F[_]: Async](
                         db: Database
                       )(implicit ec: ExecutionContext): UserModule[F] = {
    val userRepo: UserRepo[F] =
      new SlickUserRepo[F](db)

    val userService: UserService[F] =
      new UserServiceLive[F](userRepo)
    

    UserModule(
      userRepo = userRepo,
      userService = userService,
    )
  }
}