package io.github.oleksiybondar.api.modules

import cats.effect.kernel.Async
import io.github.oleksiybondar.api.config.{AuthConfig, PasswordConfig}
import io.github.oleksiybondar.api.domain.auth.{AuthService, AuthServiceLive}
import io.github.oleksiybondar.api.http.routes.rest.auth.AuthRoutes
import io.github.oleksiybondar.api.infrastructure.auth.JwtServiceLive
import io.github.oleksiybondar.api.infrastructure.auth.password.{
  PasswordHistoryLive,
  PasswordStrengthValidatorLive
}
import io.github.oleksiybondar.api.infrastructure.crypto.Password4jPasswordHasher
import io.github.oleksiybondar.api.infrastructure.db.auth.AuthSessionRepo
import io.github.oleksiybondar.api.infrastructure.db.auth.password.PasswordHistoryRepo
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo
import org.http4s.HttpRoutes

final case class AuthModule[F[_]](
    authService: AuthService[F],
    authRoutes: HttpRoutes[F]
)

object AuthModule {

  def make[F[_]: Async](
      authConfig: AuthConfig,
      passwordConfig: PasswordConfig,
      userRepo: UserRepo[F],
      authSessionRepo: AuthSessionRepo[F],
      passwordHistoryRepo: PasswordHistoryRepo[F]
  ): AuthModule[F] = {
    val passwordHasher              = new Password4jPasswordHasher[F](passwordConfig)
    val authService: AuthService[F] =
      new AuthServiceLive[F](
        userRepo,
        authSessionRepo,
        new JwtServiceLive[F](authConfig.jwtSecret),
        passwordHasher,
        new PasswordStrengthValidatorLive(passwordConfig.strength),
        new PasswordHistoryLive[F](passwordHistoryRepo, passwordHasher, passwordConfig),
        accessTokenTtlSeconds = authConfig.accessTokenTtlSeconds,
        sessionTtlDays = authConfig.sessionTtlDays
      )

    AuthModule(
      authService = authService,
      authRoutes = AuthRoutes.routes[F](authService)
    )
  }
}
