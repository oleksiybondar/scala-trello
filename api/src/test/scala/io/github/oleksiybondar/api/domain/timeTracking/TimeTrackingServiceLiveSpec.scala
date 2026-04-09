package io.github.oleksiybondar.api.domain.timeTracking

import cats.effect.IO
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.{
  BoardFixtures,
  BoardMemberFixtures,
  PermissionFixtures,
  RoleFixtures,
  TicketFixtures,
  TimeTrackingActivityFixtures,
  TimeTrackingFixtures,
  TimeTrackingServiceFixtures
}
import munit.FunSuite

import java.time.Instant
import java.util.UUID

class TimeTrackingServiceLiveSpec extends FunSuite {

  test(
    "createEntry persists an entry when the actor is a current board member and the board is active"
  ) {
    val command = CreateTimeTrackingEntryCommand(
      ticketId = TicketFixtures.sampleTicket.id,
      activityId = TimeTrackingActivityFixtures.developmentActivity.id,
      durationMinutes = TimeTrackingDurationMinutes(45),
      loggedAt = Instant.parse("2026-04-07T09:00:00Z"),
      description = Some(TimeTrackingEntryDescription("Investigated requirements in a meeting."))
    )

    val result = TimeTrackingServiceFixtures.withTimeTrackingService(
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.viewerRole.id)),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerTicketPermission)
    ) { ctx =>
      for {
        created <-
          ctx.timeTrackingService.createEntry(command, BoardMemberFixtures.sampleMember.userId)
        stored  <- created match {
                     case Some(entry) => ctx.timeTrackingRepo.findById(entry.id)
                     case None        => IO.pure(None)
                   }
      } yield (created.nonEmpty, stored.map(_.userId))
    }

    assertEquals(result, (true, Some(BoardMemberFixtures.sampleMember.userId)))
  }

  test("createEntry returns none when the actor is no longer a board member") {
    val command = CreateTimeTrackingEntryCommand(
      ticketId = TicketFixtures.sampleTicket.id,
      activityId = TimeTrackingActivityFixtures.developmentActivity.id,
      durationMinutes = TimeTrackingDurationMinutes(45),
      loggedAt = Instant.parse("2026-04-07T09:00:00Z"),
      description = None
    )

    val result = TimeTrackingServiceFixtures.withTimeTrackingService(
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard)
    )(_.timeTrackingService.createEntry(command, BoardMemberFixtures.sampleMember.userId))

    assertEquals(result, None)
  }

  test("createEntry returns none when the board is inactive") {
    val command = CreateTimeTrackingEntryCommand(
      ticketId = TicketFixtures.sampleTicket.id,
      activityId = TimeTrackingActivityFixtures.developmentActivity.id,
      durationMinutes = TimeTrackingDurationMinutes(45),
      loggedAt = Instant.parse("2026-04-07T09:00:00Z"),
      description = None
    )

    val result = TimeTrackingServiceFixtures.withTimeTrackingService(
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.dashboard(active = false)),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.viewerRole.id)),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerTicketPermission)
    )(_.timeTrackingService.createEntry(command, BoardMemberFixtures.sampleMember.userId))

    assertEquals(result, None)
  }

  test("getOwnEntry returns the entry even when the actor is no longer a board member") {
    val entry = TimeTrackingFixtures.sampleEntry

    val result = TimeTrackingServiceFixtures.withTimeTrackingService(
      entries = List(entry),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard)
    )(_.timeTrackingService.getOwnEntry(entry.id, entry.userId))

    assertEquals(result, Some(entry))
  }

  test("listOwnEntries returns only entries owned by the actor") {
    val owned = TimeTrackingFixtures.sampleEntry
    val other =
      TimeTrackingFixtures.entry(
        id = TimeTrackingEntryId(2),
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
      )

    val result = TimeTrackingServiceFixtures.withTimeTrackingService(
      entries = List(owned, other)
    )(_.timeTrackingService.listOwnEntries(owned.userId))

    assertEquals(result, List(owned))
  }

  test(
    "updateOwnEntry updates an owned entry when the actor is still a board member and the board is active"
  ) {
    val existing = TimeTrackingFixtures.sampleEntry
    val command  = UpdateTimeTrackingEntryCommand(
      activityId = TimeTrackingActivityFixtures.testingActivity.id,
      durationMinutes = TimeTrackingDurationMinutes(120),
      loggedAt = Instant.parse("2026-04-07T11:00:00Z"),
      description = Some(TimeTrackingEntryDescription("Updated after refining the worklog."))
    )

    val result = TimeTrackingServiceFixtures.withTimeTrackingService(
      entries = List(existing),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.viewerRole.id)),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerTicketPermission)
    ) { ctx =>
      for {
        updated <- ctx.timeTrackingService.updateOwnEntry(existing.id, command, existing.userId)
        stored  <- ctx.timeTrackingRepo.findById(existing.id)
      } yield (updated, stored.map(_.durationMinutes))
    }

    assertEquals(result, (true, Some(TimeTrackingDurationMinutes(120))))
  }

  test("updateOwnEntry returns false when the actor is no longer a board member") {
    val existing = TimeTrackingFixtures.sampleEntry
    val command  = UpdateTimeTrackingEntryCommand(
      activityId = TimeTrackingActivityFixtures.testingActivity.id,
      durationMinutes = TimeTrackingDurationMinutes(120),
      loggedAt = Instant.parse("2026-04-07T11:00:00Z"),
      description = None
    )

    val result = TimeTrackingServiceFixtures.withTimeTrackingService(
      entries = List(existing),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard)
    ) { ctx =>
      for {
        updated <- ctx.timeTrackingService.updateOwnEntry(existing.id, command, existing.userId)
        stored  <- ctx.timeTrackingRepo.findById(existing.id)
      } yield (updated, stored)
    }

    assertEquals(result, (false, Some(existing)))
  }

  test("deleteOwnEntry returns false when the board is inactive") {
    val existing = TimeTrackingFixtures.sampleEntry

    val result = TimeTrackingServiceFixtures.withTimeTrackingService(
      entries = List(existing),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.dashboard(active = false)),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.viewerRole.id)),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerTicketPermission)
    ) { ctx =>
      for {
        deleted <- ctx.timeTrackingService.deleteOwnEntry(existing.id, existing.userId)
        stored  <- ctx.timeTrackingRepo.findById(existing.id)
      } yield (deleted, stored.nonEmpty)
    }

    assertEquals(result, (false, true))
  }

  test(
    "deleteOwnEntry removes an owned entry when the actor is still a board member and the board is active"
  ) {
    val existing = TimeTrackingFixtures.sampleEntry

    val result = TimeTrackingServiceFixtures.withTimeTrackingService(
      entries = List(existing),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.viewerRole.id)),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerTicketPermission)
    ) { ctx =>
      for {
        deleted <- ctx.timeTrackingService.deleteOwnEntry(existing.id, existing.userId)
        stored  <- ctx.timeTrackingRepo.findById(existing.id)
      } yield (deleted, stored)
    }

    assertEquals(result, (true, None))
  }

  test("deleteOwnEntry returns false when trying to delete another user's entry") {
    val existing  = TimeTrackingFixtures.sampleEntry
    val otherUser = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))

    val result = TimeTrackingServiceFixtures.withTimeTrackingService(
      entries = List(existing),
      tickets = List(TicketFixtures.sampleTicket),
      boards = List(BoardFixtures.sampleDashboard),
      members = List(
        BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.viewerRole.id),
        BoardMemberFixtures.member(userId = otherUser, roleId = RoleFixtures.viewerRole.id)
      ),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerTicketPermission)
    ) { ctx =>
      for {
        deleted <- ctx.timeTrackingService.deleteOwnEntry(existing.id, otherUser)
        stored  <- ctx.timeTrackingRepo.findById(existing.id)
      } yield (deleted, stored)
    }

    assertEquals(result, (false, Some(existing)))
  }
}
