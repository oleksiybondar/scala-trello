package io.github.oleksiybondar.api.testkit.support

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.auth.{AuthSession, RefreshToken, SessionId}
import io.github.oleksiybondar.api.infrastructure.db.auth.AuthSessionRepo

import java.time.Instant

final class InMemoryAuthRepo[F[_]: Sync] private (
    val sessions: Ref[F, Map[SessionId, AuthSession]]
) extends AuthSessionRepo[F] {

  override def create(session: AuthSession): F[Unit] =
    sessions.update(_ + (session.id -> session))

  override def findActiveByRefreshToken(
      refreshToken: RefreshToken,
      now: Instant
  ): F[Option[AuthSession]] =
    sessions.get.map(
      _.values.find(session =>
        session.refreshToken == refreshToken &&
          session.revokedAt.isEmpty &&
          session.expiresAt.isAfter(now)
      )
    )

  override def rotateRefreshToken(
      sessionId: SessionId,
      currentRefreshToken: RefreshToken,
      nextRefreshToken: RefreshToken,
      now: Instant
  ): F[Boolean] =
    sessions.modify { currentSessions =>
      currentSessions.get(sessionId) match {
        case Some(session)
            if session.refreshToken == currentRefreshToken &&
              session.revokedAt.isEmpty &&
              session.expiresAt.isAfter(now) =>
          val updatedSession = session.copy(refreshToken = nextRefreshToken)
          (currentSessions.updated(sessionId, updatedSession), true)

        case _ =>
          (currentSessions, false)
      }
    }

  override def revokeByRefreshToken(refreshToken: RefreshToken, revokedAt: Instant): F[Boolean] =
    sessions.modify { currentSessions =>
      val matchingSessionIds = currentSessions.collect {
        case (sessionId, session)
            if session.refreshToken == refreshToken && session.revokedAt.isEmpty =>
          sessionId
      }

      val updatedSessions =
        matchingSessionIds.foldLeft(currentSessions) { case (sessionsMap, sessionId) =>
          sessionsMap.updatedWith(sessionId)(_.map(_.copy(revokedAt = Some(revokedAt))))
        }

      (updatedSessions, matchingSessionIds.nonEmpty)
    }
}

object InMemoryAuthRepo {

  def create[F[_]: Sync](): F[InMemoryAuthRepo[F]] =
    Ref.of[F, Map[SessionId, AuthSession]](Map.empty).map(new InMemoryAuthRepo[F](_))
}
