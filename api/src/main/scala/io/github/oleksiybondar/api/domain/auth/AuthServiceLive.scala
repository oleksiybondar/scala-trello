package io.github.oleksiybondar.api.domain.auth

import cats.data.EitherT
import cats.effect.Clock
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.auth.AuthError.{InvalidCredentials, InvalidRefreshToken}
import io.github.oleksiybondar.api.domain.user.{Email, User, Username}
import io.github.oleksiybondar.api.infrastructure.db.auth.AuthSessionRepo
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

final class AuthServiceLive[F[_]: Sync: Clock](
    userRepo: UserRepo[F],
    authSessionRepo: AuthSessionRepo[F],
    jwtService: JwtService[F],
    accessTokenTtlSeconds: Long,
    sessionTtlDays: Long
) extends AuthService[F] {

  override def login(login: String, password: String): EitherT[F, AuthError, AuthTokens] =
    for {
      user   <- EitherT.fromOptionF(findUser(login, password), InvalidCredentials)
      tokens <- EitherT.liftF(issueTokens(user))
    } yield tokens

  override def refresh(refreshToken: RefreshToken): EitherT[F, AuthError, AuthTokens] =
    for {
      now     <- EitherT.liftF(currentTime)
      session <- EitherT.fromOptionF(
                   authSessionRepo.findActiveByRefreshToken(refreshToken, now),
                   InvalidRefreshToken
                 )
      next    <- EitherT.liftF(randomRefreshToken)
      rotated <- EitherT.liftF(
                   authSessionRepo.rotateRefreshToken(session.id, refreshToken, next, now)
                 )
      tokens  <- if (rotated) {
                   EitherT.liftF(issueAccessTokens(session.userId, session.id, next, now))
                 } else {
                   EitherT.leftT[F, AuthTokens](InvalidRefreshToken)
                 }
    } yield tokens

  override def logout(refreshToken: RefreshToken): F[Unit] =
    currentTime.flatMap(now => authSessionRepo.revokeByRefreshToken(refreshToken, now)).void

  override def verifyToken(accessToken: AccessToken)
      : F[Option[io.github.oleksiybondar.api.domain.user.UserId]] =
    jwtService.verify(accessToken).map(_.map(_.userId))

  private def findUser(login: String, password: String): F[Option[User]] = {
    val _ = password
    if (login.contains("@")) {
      userRepo.findByEmail(Email(login))
    } else {
      userRepo.findByUsername(Username(login))
    }
  }

  private def issueTokens(user: User): F[AuthTokens] =
    for {
      now          <- currentTime
      sessionId    <- randomSessionId
      refreshToken <- randomRefreshToken
      session       = AuthSession(
                        id = sessionId,
                        userId = user.id,
                        refreshToken = refreshToken,
                        createdAt = now,
                        revokedAt = None,
                        expiresAt = now.plus(sessionTtlDays, ChronoUnit.DAYS)
                      )
      _            <- authSessionRepo.create(session)
      tokens       <- issueAccessTokens(user.id, sessionId, refreshToken, now)
    } yield tokens

  private def issueAccessTokens(
      userId: io.github.oleksiybondar.api.domain.user.UserId,
      sessionId: SessionId,
      refreshToken: RefreshToken,
      now: Instant
  ): F[AuthTokens] =
    jwtService
      .encode(
        AccessTokenClaims(
          userId = userId,
          sessionId = sessionId,
          tokenId = UUID.randomUUID(),
          issuedAt = now,
          expiresAt = now.plusSeconds(accessTokenTtlSeconds)
        )
      )
      .map(accessToken =>
        AuthTokens(
          accessToken = accessToken,
          refreshToken = refreshToken,
          tokenType = "Bearer",
          expiresIn = accessTokenTtlSeconds
        )
      )

  private def currentTime: F[Instant] =
    Clock[F].realTimeInstant

  private def randomSessionId: F[SessionId] =
    Sync[F].delay(SessionId(UUID.randomUUID()))

  private def randomRefreshToken: F[RefreshToken] =
    Sync[F].delay(RefreshToken(UUID.randomUUID()))
}
