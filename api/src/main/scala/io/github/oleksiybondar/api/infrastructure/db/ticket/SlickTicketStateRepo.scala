package io.github.oleksiybondar.api.infrastructure.db.ticket

import cats.effect.Async
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.ticket.{TicketState, TicketStateId, TicketStateName}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

final class SlickTicketStateRepo[F[_]: Async](db: Database) extends TicketStateRepo[F] {

  private final case class TicketStateRow(
      id: Long,
      name: String,
      description: Option[String]
  )

  private final class StatesTable(tag: Tag) extends Table[TicketStateRow](tag, "states") {
    def id: Rep[Long]                    = column[Long]("id", O.PrimaryKey)
    def name: Rep[String]                = column[String]("name")
    def description: Rep[Option[String]] = column[Option[String]]("description")

    def * : ProvenShape[TicketStateRow] =
      (id, name, description).mapTo[TicketStateRow]
  }

  private val states = TableQuery[StatesTable]

  private def toDomain(row: TicketStateRow): TicketState =
    TicketState(
      id = TicketStateId(row.id),
      name = TicketStateName(row.name),
      description = row.description
    )

  private def run[A](action: DBIO[A]): F[A] =
    Async[F].fromFuture(Async[F].delay(db.run(action)))

  override def findById(id: TicketStateId): F[Option[TicketState]] =
    run(
      states
        .filter(_.id === id.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def findByName(name: TicketStateName): F[Option[TicketState]] =
    run(
      states
        .filter(_.name === name.value)
        .result
        .headOption
    ).map(_.map(toDomain))

  override def list: F[List[TicketState]] =
    run(
      states
        .sortBy(_.id.asc)
        .result
    ).map(_.toList.map(toDomain))
}
