package io.github.oleksiybondar.api.infrastructure.auth.password

import cats.effect.kernel.{Clock, Sync}
import cats.syntax.all._
import io.github.oleksiybondar.api.config.PasswordConfig
import io.github.oleksiybondar.api.domain.auth.password.{
  PasswordHasher,
  PasswordHistory,
  PasswordHistoryEntry,
  PasswordHistoryId
}
import io.github.oleksiybondar.api.domain.user.{PasswordHash, UserId}
import io.github.oleksiybondar.api.infrastructure.db.auth.password.PasswordHistoryRepo

import java.util.UUID

final class PasswordHistoryLive[F[_]: Sync: Clock](
    passwordHistoryRepo: PasswordHistoryRepo[F],
    passwordHasher: PasswordHasher[F],
    passwordConfig: PasswordConfig
) extends PasswordHistory[F] {

  override def record(userId: UserId, hash: PasswordHash): F[Unit] =
    for {
      now     <- Clock[F].realTimeInstant
      entryId <- Sync[F].delay(PasswordHistoryId(UUID.randomUUID()))
      _       <- passwordHistoryRepo.create(
                   PasswordHistoryEntry(
                     id = entryId,
                     userId = userId,
                     passwordHash = hash,
                     createdAt = now
                   )
                 )
      _       <- pruneHistory(userId)
    } yield ()

  override def wasUsedBefore(userId: UserId, password: String): F[Boolean] =
    passwordHistoryRepo
      .findByUserId(userId)
      .flatMap(_.existsM(entry => passwordHasher.verify(password, entry.passwordHash)))

  override def clear(userId: UserId): F[Unit] =
    passwordHistoryRepo.deleteByUserId(userId)

  private def pruneHistory(userId: UserId): F[Unit] =
    passwordHistoryRepo
      .findByUserId(userId)
      .flatMap(_.drop(passwordConfig.historySize).map(_.id).pure[F])
      .flatMap(passwordHistoryRepo.deleteByIds)
}
