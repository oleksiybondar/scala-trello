package io.github.oleksiybondar.api.modules

import cats.effect.Ref
import cats.effect.kernel.Async
import io.github.oleksiybondar.api.domain.auth.{AccessToken, AuthService, AuthServiceLive, RefreshToken}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.http.routes.rest.auth.AuthRoutes
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo
import org.http4s.HttpRoutes

final case class AuthModule[F[_]](
  authService: AuthService[F],
  authRoutes: HttpRoutes[F]
)

object AuthModule {

  def make[F[_]: Async](
    userRepo: UserRepo[F],
    accessTokenStore: Ref[F, Map[AccessToken, UserId]],
    refreshTokenStore: Ref[F, Map[RefreshToken, UserId]]
  ): AuthModule[F] = {
    val authService: AuthService[F] =
      new AuthServiceLive[F](
        userRepo,
        accessTokenStore,
        refreshTokenStore
      )

    AuthModule(
      authService = authService,
      authRoutes = AuthRoutes.routes[F](authService)
    )
  }
}
