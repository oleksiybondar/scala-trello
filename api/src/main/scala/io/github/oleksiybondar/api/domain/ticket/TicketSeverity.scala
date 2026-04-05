package io.github.oleksiybondar.api.domain.ticket

final case class TicketSeverityId(value: Long)     extends AnyVal
final case class TicketSeverityName(value: String) extends AnyVal

final case class TicketSeverity(
    id: TicketSeverityId,
    name: TicketSeverityName,
    description: Option[String]
)
