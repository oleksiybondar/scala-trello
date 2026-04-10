package io.github.oleksiybondar.api.modules

import cats.effect.Async
import io.github.oleksiybondar.api.config.PasswordConfig
import io.github.oleksiybondar.api.domain.user.{UserService, UserServiceLive}
import io.github.oleksiybondar.api.infrastructure.auth.password.{
  PasswordHistoryLive,
  PasswordStrengthValidatorLive
}
import io.github.oleksiybondar.api.infrastructure.crypto.Password4jPasswordHasher
import io.github.oleksiybondar.api.infrastructure.db.auth.password.{
  PasswordHistoryRepo,
  PasswordHistoryRepoSlick
}
import io.github.oleksiybondar.api.infrastructure.db.user.{SlickUserRepo, UserRepo}
import slick.jdbc.PostgresProfile.api.Database

final case class UserModule[F[_]](
    userRepo: UserRepo[F],
    passwordHistoryRepo: PasswordHistoryRepo[F],
    userService: UserService[F]
)

object UserModule {

  def make[F[_]: Async](
      passwordConfig: PasswordConfig,
      db: Database
  ): UserModule[F] = {
    val userRepo            = new SlickUserRepo[F](db)
    val passwordHistoryRepo = new PasswordHistoryRepoSlick[F](db)
    val passwordHasher      = new Password4jPasswordHasher[F](passwordConfig)

    UserModule(
      userRepo = userRepo,
      passwordHistoryRepo = passwordHistoryRepo,
      userService = new UserServiceLive[F](
        userRepo,
        passwordHasher,
        new PasswordStrengthValidatorLive(passwordConfig.strength),
        new PasswordHistoryLive[F](passwordHistoryRepo, passwordHasher, passwordConfig)
      )
    )
  }
}
