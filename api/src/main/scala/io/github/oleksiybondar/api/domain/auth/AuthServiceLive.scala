package io.github.oleksiybondar.api.domain.auth

import cats.data.EitherT
import cats.effect.Clock
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.auth.AuthError.{
  EmailAlreadyUsed,
  EmailRequired,
  InvalidCredentials,
  InvalidEmail,
  InvalidRefreshToken,
  WeakPassword
}
import io.github.oleksiybondar.api.domain.auth.password.{
  PasswordHasher,
  PasswordHistory,
  PasswordStrengthValidator
}
import io.github.oleksiybondar.api.domain.user.{Email, FirstName, LastName, User, UserId, Username}
import io.github.oleksiybondar.api.infrastructure.db.auth.AuthSessionRepo
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo
import io.github.oleksiybondar.api.validation.InputValidation

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

final class AuthServiceLive[F[_]: Sync: Clock](
    userRepo: UserRepo[F],
    authSessionRepo: AuthSessionRepo[F],
    jwtService: JwtService[F],
    passwordHasher: PasswordHasher[F],
    passwordStrengthValidator: PasswordStrengthValidator,
    passwordHistory: PasswordHistory[F],
    accessTokenTtlSeconds: Long,
    sessionTtlDays: Long
) extends AuthService[F] {

  // In this project registration intentionally acts as a registration + login shortcut.
  override def register(command: RegisterUserCommand): EitherT[F, AuthError, AuthTokens] =
    for {
      normalizedEmail <- EitherT.fromEither[F](normalizeEmail(command.email))
      _               <- EitherT(
                           userRepo
                             .findByEmail(normalizedEmail)
                             .map(_.fold[Either[AuthError, Unit]](Right(()))(_ =>
                               Left(EmailAlreadyUsed)
                             ))
                         )
      _               <- EitherT.fromEither[F](
                           passwordStrengthValidator
                             .validate(command.password)
                             .leftMap(errors => WeakPassword(errors.toChain.toList))
                             .toEither
                         )
      passwordHash    <- EitherT.liftF(passwordHasher.hash(command.password))
      now             <- EitherT.liftF(currentTime)
      userId          <- EitherT.liftF(randomUserId)
      user             = User(
                           id = userId,
                           username = command.username,
                           email = Some(normalizedEmail),
                           passwordHash = passwordHash,
                           firstName = FirstName(command.firstName),
                           lastName = LastName(command.lastName),
                           avatarUrl = None,
                           createdAt = now
                         )
      _               <- EitherT.liftF(userRepo.create(user))
      _               <- EitherT.liftF(passwordHistory.record(user.id, passwordHash))
      tokens          <- EitherT.liftF(issueTokens(user))
    } yield tokens

  override def login(login: String, password: String): EitherT[F, AuthError, AuthTokens] =
    for {
      user   <- EitherT.fromOptionF(findUser(login), InvalidCredentials)
      valid  <- EitherT.liftF(passwordHasher.verify(password, user.passwordHash))
      _      <- EitherT.cond[F](valid, (), InvalidCredentials)
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

  private def findUser(login: String): F[Option[User]] =
    if (login.contains("@")) {
      userRepo.findByEmail(Email(login.trim.toLowerCase))
    } else {
      userRepo.findByUsername(Username(login.trim))
    }

  private def normalizeEmail(rawEmail: String): Either[AuthError, Email] = {
    InputValidation.normalizeEmail(rawEmail) match {
      case None                                                          => Left(EmailRequired)
      case Some(normalized) if !InputValidation.isValidEmail(normalized) =>
        Left(InvalidEmail)
      case Some(normalized)                                              => Right(Email(normalized))
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

  private def randomUserId: F[UserId] =
    Sync[F].delay(UserId(UUID.randomUUID()))
}
