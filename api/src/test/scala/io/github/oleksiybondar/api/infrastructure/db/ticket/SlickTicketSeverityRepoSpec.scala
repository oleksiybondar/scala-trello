package io.github.oleksiybondar.api.infrastructure.db.ticket

import io.github.oleksiybondar.api.domain.ticket.{TicketSeverityId, TicketSeverityName}
import io.github.oleksiybondar.api.testkit.fixtures.SlickTicketSeverityRepoFixtures.withRepo
import io.github.oleksiybondar.api.testkit.fixtures.TicketSeverityFixtures
import munit.FunSuite

class SlickTicketSeverityRepoSpec extends FunSuite {

  test("findById returns the seeded severity") {
    withRepo { repo =>
      repo.findById(TicketSeverityId(1)).map { result =>
        assertEquals(result, Some(TicketSeverityFixtures.minorSeverity))
      }
    }
  }

  test("findByName returns the matching seeded severity") {
    withRepo { repo =>
      repo.findByName(TicketSeverityName("major")).map { result =>
        assertEquals(result, Some(TicketSeverityFixtures.majorSeverity))
      }
    }
  }

  test("list returns all seeded severities in id order") {
    withRepo { repo =>
      repo.list.map { result =>
        assertEquals(
          result,
          List(
            TicketSeverityFixtures.minorSeverity,
            TicketSeverityFixtures.normalSeverity,
            TicketSeverityFixtures.majorSeverity
          )
        )
      }
    }
  }
}
