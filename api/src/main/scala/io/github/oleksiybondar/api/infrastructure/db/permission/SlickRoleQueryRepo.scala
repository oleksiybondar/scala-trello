package io.github.oleksiybondar.api.infrastructure.db.permission

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.permission.{
  Permission,
  PermissionArea,
  PermissionId,
  Role,
  RoleId,
  RoleName,
  RoleWithPermissions
}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

final class SlickRoleQueryRepo[F[_]: Async](db: Database) extends RoleQueryRepo[F] {

  private final case class RoleRow(
      id: Long,
      name: String,
      description: Option[String]
  )

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

  private final class RolesTable(tag: Tag) extends Table[RoleRow](tag, "roles") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[RoleRow] = (id, name, description).mapTo[RoleRow]
  }

  private final class PermissionsTable(tag: Tag)
      extends Table[PermissionRow](tag, "permissions") {
    def id: Rep[Long]             = column[Long]("id", O.PrimaryKey)
    def roleId: Rep[Long]         = column[Long]("role_id")
    def area: Rep[String]         = column[String]("area")
    def canRead: Rep[Boolean]     = column[Boolean]("can_read")
    def canCreate: Rep[Boolean]   = column[Boolean]("can_create")
    def canModify: Rep[Boolean]   = column[Boolean]("can_modify")
    def canDelete: Rep[Boolean]   = column[Boolean]("can_delete")
    def canReassign: Rep[Boolean] = column[Boolean]("can_reassign")

    def * : ProvenShape[PermissionRow] =
      (id, roleId, area, canRead, canCreate, canModify, canDelete, canReassign).mapTo[PermissionRow]
  }

  private val roles       = TableQuery[RolesTable]
  private val permissions = TableQuery[PermissionsTable]

  override def findById(id: RoleId): F[Option[RoleWithPermissions]] =
    run(
      roles
        .filter(_.id === id.value)
        .joinLeft(permissions)
        .on(_.id === _.roleId)
        .sortBy(_._2.map(_.id).asc)
        .result
    ).flatMap(rows => decodeRows(rows).map(_.headOption))

  override def list: F[List[RoleWithPermissions]] =
    run(
      roles
        .joinLeft(permissions)
        .on(_.id === _.roleId)
        .sortBy { case (role, permission) => (role.id.asc, permission.map(_.id).asc) }
        .result
    ).flatMap(decodeRows)

  private def decodeRows(
      rows: Seq[(RoleRow, Option[PermissionRow])]
  ): F[List[RoleWithPermissions]] =
    rows.toList
      .groupBy(_._1.id)
      .toList
      .sortBy(_._1)
      .traverse { case (_, groupedRows) =>
        val roleRow = groupedRows.head._1
        groupedRows.traverse(_._2.traverse(decodePermission)).map { decodedPermissions =>
          RoleWithPermissions(
            role = Role(
              id = RoleId(roleRow.id),
              name = RoleName(roleRow.name),
              description = roleRow.description
            ),
            permissions = decodedPermissions.flatten
          )
        }
      }

  private def decodePermission(row: PermissionRow): F[Permission] =
    Async[F].fromEither(
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
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))
}
