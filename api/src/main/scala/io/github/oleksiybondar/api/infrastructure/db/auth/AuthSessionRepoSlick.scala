package io.github.oleksiybondar.api.infrastructure.db.auth

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.auth.{AuthSession, RefreshToken, SessionId}
import io.github.oleksiybondar.api.domain.user.UserId
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

final class AuthSessionRepoSlick[F[_]: Async](
    db: Database
) extends AuthSessionRepo[F] {

  private final case class AuthSessionRow(
      id: UUID,
      refreshToken: UUID,
      userId: UUID,
      createdAt: Instant,
      revokedAt: Option[Instant],
      expiresAt: Instant
  )

  private final class AuthSessionsTable(tag: Tag)
      extends Table[AuthSessionRow](tag, "auth_sessions") {
    def id: Rep[UUID]                   = column[UUID]("id", O.PrimaryKey)
    def refreshToken: Rep[UUID]         = column[UUID]("refresh_token")
    def userId: Rep[UUID]               = column[UUID]("user_id")
    def createdAt: Rep[Instant]         = column[Instant]("created_at")
    def revokedAt: Rep[Option[Instant]] = column[Option[Instant]]("revoked_at")
    def expiresAt: Rep[Instant]         = column[Instant]("expires_at")

    def * : ProvenShape[AuthSessionRow] =
      (
        id,
        refreshToken,
        userId,
        createdAt,
        revokedAt,
        expiresAt
      ).mapTo[AuthSessionRow]
  }

  private val authSessions = TableQuery[AuthSessionsTable]

  override def create(session: AuthSession): F[Unit] =
    run(authSessions += toRow(session)).void

  override def findActiveByRefreshToken(
      refreshToken: RefreshToken,
      now: Instant
  ): F[Option[AuthSession]] =
    run(
      authSessions
        .filter(session =>
          session.refreshToken === refreshToken.value &&
            session.revokedAt.isEmpty &&
            session.expiresAt > now
        )
        .result
        .headOption
    ).map(_.map(toDomain))

  override def deleteExpiredOrRevoked(now: Instant): F[Int] =
    run(
      authSessions
        .filter(session =>
          session.revokedAt.isDefined ||
            session.expiresAt <= now
        )
        .delete
    )

  override def rotateRefreshToken(
      sessionId: SessionId,
      currentRefreshToken: RefreshToken,
      nextRefreshToken: RefreshToken,
      now: Instant
  ): F[Boolean] =
    run(
      authSessions
        .filter(session =>
          session.id === sessionId.value &&
            session.refreshToken === currentRefreshToken.value &&
            session.revokedAt.isEmpty &&
            session.expiresAt > now
        )
        .map(_.refreshToken)
        .update(nextRefreshToken.value)
    ).map(_ > 0)

  override def revokeByRefreshToken(refreshToken: RefreshToken, revokedAt: Instant): F[Boolean] =
    run(
      authSessions
        .filter(session =>
          session.refreshToken === refreshToken.value &&
            session.revokedAt.isEmpty
        )
        .map(_.revokedAt)
        .update(Some(revokedAt))
    ).map(_ > 0)

  private def toRow(session: AuthSession): AuthSessionRow =
    AuthSessionRow(
      id = session.id.value,
      refreshToken = session.refreshToken.value,
      userId = session.userId.value,
      createdAt = session.createdAt,
      revokedAt = session.revokedAt,
      expiresAt = session.expiresAt
    )

  private def toDomain(row: AuthSessionRow): AuthSession =
    AuthSession(
      id = SessionId(row.id),
      refreshToken = RefreshToken(row.refreshToken),
      userId = UserId(row.userId),
      createdAt = row.createdAt,
      revokedAt = row.revokedAt,
      expiresAt = row.expiresAt
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))
}
