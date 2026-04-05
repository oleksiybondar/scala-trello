package io.github.oleksiybondar.api.infrastructure.db.permission

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.permission.{Role, RoleId, RoleName}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

final class SlickRoleRepo[F[_]: Async](db: Database) extends RoleRepo[F] {

  private final case class RoleRow(
      id: Long,
      name: String,
      description: Option[String]
  )

  private final class RolesTable(tag: Tag) extends Table[RoleRow](tag, "roles") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[RoleRow] =
      (id, name, description).mapTo[RoleRow]
  }

  private val roles = TableQuery[RolesTable]

  private def toDomain(row: RoleRow): Role =
    Role(
      id = RoleId(row.id),
      name = RoleName(row.name),
      description = row.description
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))

  override def findById(id: RoleId): F[Option[Role]] =
    run(
      roles
        .filter(_.id === id.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def findByName(name: RoleName): F[Option[Role]] =
    run(
      roles
        .filter(_.name === name.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def list: F[List[Role]] =
    run(
      roles
        .sortBy(_.id.asc)
        .result
    ).map(_.toList.map(toDomain))
}
