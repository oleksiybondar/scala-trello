package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.ticket.{TicketState, TicketStateId, TicketStateName}

object TicketStateFixtures {

  val newState: TicketState =
    TicketState(
      id = TicketStateId(1),
      name = TicketStateName("new"),
      description = Some("Ticket has been created and is awaiting work.")
    )

  val inProgressState: TicketState =
    TicketState(
      id = TicketStateId(2),
      name = TicketStateName("in_progress"),
      description = Some("Work on the ticket is currently in progress.")
    )

  val codeReviewState: TicketState =
    TicketState(
      id = TicketStateId(3),
      name = TicketStateName("code_review"),
      description = Some("Implementation is ready for review.")
    )

  val inTestingState: TicketState =
    TicketState(
      id = TicketStateId(4),
      name = TicketStateName("in_testing"),
      description = Some("Changes are being verified.")
    )

  val doneState: TicketState =
    TicketState(
      id = TicketStateId(5),
      name = TicketStateName("done"),
      description = Some("Work is completed.")
    )
}
