package io.github.oleksiybondar.api.domain.ticket

final case class TicketStateId(value: Long)     extends AnyVal
final case class TicketStateName(value: String) extends AnyVal

final case class TicketState(
    id: TicketStateId,
    name: TicketStateName,
    description: Option[String]
)
