package io.github.oleksiybondar.api.infrastructure.db.ticket

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.ticket.{
  TicketSeverity,
  TicketSeverityId,
  TicketSeverityName
}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

final class SlickTicketSeverityRepo[F[_]: Async](db: Database) extends TicketSeverityRepo[F] {

  private final case class TicketSeverityRow(
      id: Long,
      name: String,
      description: Option[String]
  )

  private final class SeveritiesTable(tag: Tag)
      extends Table[TicketSeverityRow](tag, "severities") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[TicketSeverityRow] =
      (id, name, description).mapTo[TicketSeverityRow]
  }

  private val severities = TableQuery[SeveritiesTable]

  private def toDomain(row: TicketSeverityRow): TicketSeverity =
    TicketSeverity(
      id = TicketSeverityId(row.id),
      name = TicketSeverityName(row.name),
      description = row.description
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))

  override def findById(id: TicketSeverityId): F[Option[TicketSeverity]] =
    run(
      severities
        .filter(_.id === id.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def findByName(name: TicketSeverityName): F[Option[TicketSeverity]] =
    run(
      severities
        .filter(_.name === name.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def list: F[List[TicketSeverity]] =
    run(
      severities
        .sortBy(_.id.asc)
        .result
    ).map(_.toList.map(toDomain))
}
