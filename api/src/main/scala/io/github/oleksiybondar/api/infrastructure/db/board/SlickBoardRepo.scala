package io.github.oleksiybondar.api.infrastructure.db.board

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.board.{Board, BoardDescription, BoardId, BoardName}
import io.github.oleksiybondar.api.domain.user.UserId
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

final class SlickBoardRepo[F[_]: Async](db: Database) extends BoardRepo[F] {

  private final case class BoardRow(
      id: UUID,
      name: String,
      description: Option[String],
      active: Boolean,
      ownerUserId: UUID,
      createdByUserId: UUID,
      createdAt: Instant,
      modifiedAt: Instant,
      lastModifiedByUserId: UUID
  )

  private final class BoardsTable(tag: Tag)
      extends Table[BoardRow](tag, "boards") {
    def id: Rep[UUID]                    = column[UUID]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")
    def active: Rep[Boolean]             = column[Boolean]("active")
    def ownerUserId: Rep[UUID]           = column[UUID]("owner_user_id")
    def createdByUserId: Rep[UUID]       = column[UUID]("created_by_user_id")
    def createdAt: Rep[Instant]          = column[Instant]("created_at")
    def modifiedAt: Rep[Instant]         = column[Instant]("modified_at")
    def lastModifiedByUserId: Rep[UUID]  = column[UUID]("last_modified_by_user_id")

    def * : ProvenShape[BoardRow] =
      (
        id,
        name,
        description,
        active,
        ownerUserId,
        createdByUserId,
        createdAt,
        modifiedAt,
        lastModifiedByUserId
      ).mapTo[BoardRow]
  }

  private final case class BoardMemberLookupRow(
      dashboardId: UUID,
      userId: UUID
  )

  private final class BoardMembersTable(tag: Tag)
      extends Table[BoardMemberLookupRow](tag, "dashboard_members") {
    def dashboardId: Rep[UUID] = column[UUID]("dashboard_id")
    def userId: Rep[UUID]      = column[UUID]("user_id")

    def * : ProvenShape[BoardMemberLookupRow] =
      (dashboardId, userId).mapTo[BoardMemberLookupRow]
  }

  private val boards       = TableQuery[BoardsTable]
  private val boardMembers = TableQuery[BoardMembersTable]

  private def toRow(board: Board): BoardRow =
    BoardRow(
      id = board.id.value,
      name = board.name.value,
      description = board.description.map(_.value),
      active = board.active,
      ownerUserId = board.ownerUserId.value,
      createdByUserId = board.createdByUserId.value,
      createdAt = board.createdAt,
      modifiedAt = board.modifiedAt,
      lastModifiedByUserId = board.lastModifiedByUserId.value
    )

  private def toDomain(row: BoardRow): Board =
    Board(
      id = BoardId(row.id),
      name = BoardName(row.name),
      description = row.description.map(BoardDescription(_)),
      active = row.active,
      ownerUserId = UserId(row.ownerUserId),
      createdByUserId = UserId(row.createdByUserId),
      createdAt = row.createdAt,
      modifiedAt = row.modifiedAt,
      lastModifiedByUserId = UserId(row.lastModifiedByUserId)
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))

  override def create(board: Board): F[Unit] =
    run(boards += toRow(board)).void

  override def findById(id: BoardId): F[Option[Board]] =
    run(
      boards
        .filter(_.id === id.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def list: F[List[Board]] =
    run(
      boards
        .sortBy(_.createdAt.desc)
        .result
    ).map(_.toList.map(toDomain))

  override def listByOwner(ownerUserId: UserId): F[List[Board]] =
    run(
      boards
        .filter(_.ownerUserId === ownerUserId.value)
        .sortBy(_.createdAt.desc)
        .result
    ).map(_.toList.map(toDomain))

  override def listByMember(userId: UserId): F[List[Board]] =
    run(
      boards
        .join(boardMembers)
        .on(_.id === _.dashboardId)
        .filter { case (_, member) => member.userId === userId.value }
        .sortBy { case (board, _) => board.createdAt.desc }
        .map { case (board, _) => board }
        .result
    ).map(_.toList.map(toDomain))

  override def update(board: Board): F[Boolean] =
    run(
      boards
        .filter(_.id === board.id.value)
        .update(toRow(board))
    ).map(_ > 0)

  override def delete(id: BoardId): F[Boolean] =
    run(
      boards
        .filter(_.id === id.value)
        .delete
    ).map(_ > 0)
}
