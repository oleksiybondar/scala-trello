package io.github.oleksiybondar.api.infrastructure.db.timeTracking

import io.github.oleksiybondar.api.domain.timeTracking.{
  TimeTrackingActivityCode,
  TimeTrackingActivityId
}
import io.github.oleksiybondar.api.testkit.fixtures.SlickTimeTrackingActivityRepoFixtures.withRepo
import io.github.oleksiybondar.api.testkit.fixtures.TimeTrackingActivityFixtures
import munit.FunSuite

class SlickTimeTrackingActivityRepoSpec extends FunSuite {

  test("findById returns the seeded activity") {
    withRepo { repo =>
      repo.findById(TimeTrackingActivityId(1)).map { result =>
        assertEquals(result, Some(TimeTrackingActivityFixtures.codeReviewActivity))
      }
    }
  }

  test("findByCode returns the matching seeded activity") {
    withRepo { repo =>
      repo.findByCode(TimeTrackingActivityCode("debugging")).map { result =>
        assertEquals(result, Some(TimeTrackingActivityFixtures.debuggingActivity))
      }
    }
  }

  test("list returns all seeded activities in id order") {
    withRepo { repo =>
      repo.list.map { result =>
        assertEquals(
          result,
          List(
            TimeTrackingActivityFixtures.codeReviewActivity,
            TimeTrackingActivityFixtures.developmentActivity,
            TimeTrackingActivityFixtures.testingActivity,
            TimeTrackingActivityFixtures.planningActivity,
            TimeTrackingActivityFixtures.designActivity,
            TimeTrackingActivityFixtures.documentationActivity,
            TimeTrackingActivityFixtures.refinementActivity,
            TimeTrackingActivityFixtures.debuggingActivity
          )
        )
      }
    }
  }
}
