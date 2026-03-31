package io.github.oleksiybondar.api.infrastructure.db.auth.password

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.auth.password.{PasswordHistoryEntry, PasswordHistoryId}
import io.github.oleksiybondar.api.domain.user.{PasswordHash, UserId}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

final class PasswordHistoryRepoSlick[F[_]: Async](
    db: Database
) extends PasswordHistoryRepo[F] {

  private final case class PasswordHistoryRow(
      id: UUID,
      userId: UUID,
      passwordHash: String,
      createdAt: Instant
  )

  private final class PasswordHistoryTable(tag: Tag)
      extends Table[PasswordHistoryRow](tag, "password_history") {
    def id: Rep[UUID]             = column[UUID]("id", O.PrimaryKey)
    def userId: Rep[UUID]         = column[UUID]("user_id")
    def passwordHash: Rep[String] = column[String]("password_hash")
    def createdAt: Rep[Instant]   = column[Instant]("created_at")

    def * : ProvenShape[PasswordHistoryRow] =
      (
        id,
        userId,
        passwordHash,
        createdAt
      ).mapTo[PasswordHistoryRow]
  }

  private val passwordHistory = TableQuery[PasswordHistoryTable]

  override def create(entry: PasswordHistoryEntry): F[Unit] =
    run(passwordHistory += toRow(entry)).void

  override def findByUserId(userId: UserId): F[List[PasswordHistoryEntry]] =
    run(
      passwordHistory
        .filter(_.userId === userId.value)
        .sortBy(_.createdAt.desc)
        .result
    ).map(_.toList.map(toDomain))

  override def deleteByUserId(userId: UserId): F[Unit] =
    run(
      passwordHistory
        .filter(_.userId === userId.value)
        .delete
    ).void

  override def deleteByIds(ids: List[PasswordHistoryId]): F[Unit] =
    ids match {
      case Nil    => Async[F].unit
      case values =>
        run(
          passwordHistory
            .filter(_.id.inSet(values.map(_.value)))
            .delete
        ).void
    }

  private def toRow(entry: PasswordHistoryEntry): PasswordHistoryRow =
    PasswordHistoryRow(
      id = entry.id.value,
      userId = entry.userId.value,
      passwordHash = entry.passwordHash.value,
      createdAt = entry.createdAt
    )

  private def toDomain(row: PasswordHistoryRow): PasswordHistoryEntry =
    PasswordHistoryEntry(
      id = PasswordHistoryId(row.id),
      userId = UserId(row.userId),
      passwordHash = PasswordHash(row.passwordHash),
      createdAt = row.createdAt
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))
}
