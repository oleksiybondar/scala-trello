package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.ticket.{
  TicketSeverity,
  TicketSeverityId,
  TicketSeverityName
}

object TicketSeverityFixtures {

  val minorSeverity: TicketSeverity =
    TicketSeverity(
      id = TicketSeverityId(1),
      name = TicketSeverityName("minor"),
      description = Some("Low impact issue or task.")
    )

  val normalSeverity: TicketSeverity =
    TicketSeverity(
      id = TicketSeverityId(2),
      name = TicketSeverityName("normal"),
      description = Some("Standard impact issue or task.")
    )

  val majorSeverity: TicketSeverity =
    TicketSeverity(
      id = TicketSeverityId(3),
      name = TicketSeverityName("major"),
      description = Some("High impact issue or task.")
    )
}
