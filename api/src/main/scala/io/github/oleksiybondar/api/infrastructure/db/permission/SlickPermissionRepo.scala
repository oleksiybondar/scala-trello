package io.github.oleksiybondar.api.infrastructure.db.permission

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.permission.{
  Permission,
  PermissionArea,
  PermissionId,
  RoleId
}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

final class SlickPermissionRepo[F[_]: Async](db: Database)
    extends PermissionRepo[F] {

  private final case class PermissionRow(
      id: Long,
      roleId: Long,
      area: String,
      canRead: Boolean,
      canCreate: Boolean,
      canModify: Boolean,
      canDelete: Boolean,
      canReassign: Boolean
  )

  private final class PermissionsTable(tag: Tag)
      extends Table[PermissionRow](tag, "permissions") {
    def id: Rep[Long]             = column[Long]("id", O.PrimaryKey)
    def roleId: Rep[Long]         = column[Long]("role_id")
    def area: Rep[String]         = column[String]("area")
    def canRead: Rep[Boolean]     = column[Boolean]("can_read")
    def canCreate: Rep[Boolean]   = column[Boolean]("can_create")
    def canModify: Rep[Boolean]   = column[Boolean]("can_modify")
    def canDelete: Rep[Boolean]   = column[Boolean]("can_delete")
    def canReassign: Rep[Boolean] =
      column[Boolean]("can_reassign")

    def * : ProvenShape[PermissionRow] =
      (id, roleId, area, canRead, canCreate, canModify, canDelete, canReassign)
        .mapTo[PermissionRow]
  }

  private val permissions = TableQuery[PermissionsTable]

  private def toDomain(
      row: PermissionRow
  ): Either[IllegalArgumentException, Permission] =
    PermissionArea
      .fromString(row.area)
      .toRight(new IllegalArgumentException(s"Unknown permission area: ${row.area}"))
      .map { area =>
        Permission(
          id = PermissionId(row.id),
          roleId = RoleId(row.roleId),
          area = area,
          canRead = row.canRead,
          canCreate = row.canCreate,
          canModify = row.canModify,
          canDelete = row.canDelete,
          canReassign = row.canReassign
        )
      }

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))

  private def decodeRow(row: PermissionRow): F[Permission] =
    Async[F].fromEither(toDomain(row))

  private def decodeRows(rows: Seq[PermissionRow]): F[List[Permission]] =
    rows.toList.traverse(decodeRow)

  override def findById(id: PermissionId): F[Option[Permission]] =
    run(
      permissions
        .filter(_.id === id.value)
        .result
        .headOption
    ).flatMap(_.traverse(decodeRow))

  override def findByRoleId(roleId: RoleId): F[List[Permission]] =
    run(
      permissions
        .filter(_.roleId === roleId.value)
        .sortBy(_.id.asc)
        .result
    ).flatMap(decodeRows)

  override def findByRoleIdAndArea(
      roleId: RoleId,
      area: PermissionArea
  ): F[Option[Permission]] =
    run(
      permissions
        .filter(row => row.roleId === roleId.value && row.area === area.value)
        .result
        .headOption
    ).flatMap(_.traverse(decodeRow))

  override def list: F[List[Permission]] =
    run(
      permissions
        .sortBy(_.id.asc)
        .result
    ).flatMap(decodeRows)
}
