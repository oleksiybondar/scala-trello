package io.github.oleksiybondar.api.infrastructure.db.ticket

import io.github.oleksiybondar.api.domain.ticket.{TicketStateId, TicketStateName}
import io.github.oleksiybondar.api.testkit.fixtures.SlickTicketStateRepoFixtures.withRepo
import io.github.oleksiybondar.api.testkit.fixtures.TicketStateFixtures
import munit.FunSuite

class SlickTicketStateRepoSpec extends FunSuite {

  test("findById returns the seeded state") {
    withRepo { repo =>
      repo.findById(TicketStateId(1)).map { result =>
        assertEquals(result, Some(TicketStateFixtures.newState))
      }
    }
  }

  test("findByName returns the matching seeded state") {
    withRepo { repo =>
      repo.findByName(TicketStateName("code_review")).map { result =>
        assertEquals(result, Some(TicketStateFixtures.codeReviewState))
      }
    }
  }

  test("list returns all seeded states in id order") {
    withRepo { repo =>
      repo.list.map { result =>
        assertEquals(
          result,
          List(
            TicketStateFixtures.newState,
            TicketStateFixtures.inProgressState,
            TicketStateFixtures.codeReviewState,
            TicketStateFixtures.inTestingState,
            TicketStateFixtures.doneState
          )
        )
      }
    }
  }
}
