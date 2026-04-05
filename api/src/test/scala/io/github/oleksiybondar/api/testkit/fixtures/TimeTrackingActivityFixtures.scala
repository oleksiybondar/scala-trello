package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.timeTracking.{
  TimeTrackingActivity,
  TimeTrackingActivityCode,
  TimeTrackingActivityId,
  TimeTrackingActivityName
}

object TimeTrackingActivityFixtures {

  val codeReviewActivity: TimeTrackingActivity =
    TimeTrackingActivity(
      id = TimeTrackingActivityId(1),
      code = TimeTrackingActivityCode("code_review"),
      name = TimeTrackingActivityName("Code Review"),
      description = Some("Reviewing implementation changes.")
    )

  val developmentActivity: TimeTrackingActivity =
    TimeTrackingActivity(
      id = TimeTrackingActivityId(2),
      code = TimeTrackingActivityCode("development"),
      name = TimeTrackingActivityName("Development"),
      description = Some("Implementing product or technical changes.")
    )

  val testingActivity: TimeTrackingActivity =
    TimeTrackingActivity(
      id = TimeTrackingActivityId(3),
      code = TimeTrackingActivityCode("testing"),
      name = TimeTrackingActivityName("Testing"),
      description = Some("Verifying behavior through testing activities.")
    )

  val planningActivity: TimeTrackingActivity =
    TimeTrackingActivity(
      id = TimeTrackingActivityId(4),
      code = TimeTrackingActivityCode("planning"),
      name = TimeTrackingActivityName("Planning"),
      description = Some("Planning or task breakdown work.")
    )

  val designActivity: TimeTrackingActivity =
    TimeTrackingActivity(
      id = TimeTrackingActivityId(5),
      code = TimeTrackingActivityCode("design"),
      name = TimeTrackingActivityName("Design"),
      description = Some("Technical or product design work.")
    )

  val documentationActivity: TimeTrackingActivity =
    TimeTrackingActivity(
      id = TimeTrackingActivityId(6),
      code = TimeTrackingActivityCode("documentation"),
      name = TimeTrackingActivityName("Documentation"),
      description = Some("Writing or updating documentation.")
    )

  val refinementActivity: TimeTrackingActivity =
    TimeTrackingActivity(
      id = TimeTrackingActivityId(7),
      code = TimeTrackingActivityCode("refinement"),
      name = TimeTrackingActivityName("Refinement"),
      description = Some("Refining scope or requirements.")
    )

  val debuggingActivity: TimeTrackingActivity =
    TimeTrackingActivity(
      id = TimeTrackingActivityId(8),
      code = TimeTrackingActivityCode("debugging"),
      name = TimeTrackingActivityName("Debugging"),
      description = Some("Diagnosing and fixing issues.")
    )
}
