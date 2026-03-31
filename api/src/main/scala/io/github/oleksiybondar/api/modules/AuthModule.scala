package io.github.oleksiybondar.api.modules

import cats.effect.kernel.Async
import io.github.oleksiybondar.api.config.AuthConfig
import io.github.oleksiybondar.api.domain.auth.{AuthService, AuthServiceLive, JwtServiceLive}
import io.github.oleksiybondar.api.http.routes.rest.auth.AuthRoutes
import io.github.oleksiybondar.api.infrastructure.db.auth.AuthSessionRepo
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo
import org.http4s.HttpRoutes

final case class AuthModule[F[_]](
    authService: AuthService[F],
    authRoutes: HttpRoutes[F]
)

object AuthModule {

  def make[F[_]: Async](
      authConfig: AuthConfig,
      userRepo: UserRepo[F],
      authSessionRepo: AuthSessionRepo[F]
  ): AuthModule[F] = {
    val authService: AuthService[F] =
      new AuthServiceLive[F](
        userRepo,
        authSessionRepo,
        new JwtServiceLive[F](authConfig.jwtSecret),
        accessTokenTtlSeconds = authConfig.accessTokenTtlSeconds,
        sessionTtlDays = authConfig.sessionTtlDays
      )

    AuthModule(
      authService = authService,
      authRoutes = AuthRoutes.routes[F](authService)
    )
  }
}
