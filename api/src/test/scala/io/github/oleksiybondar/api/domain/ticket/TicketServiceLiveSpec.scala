package io.github.oleksiybondar.api.domain.ticket

import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.{
  BoardFixtures,
  BoardMemberFixtures,
  PermissionFixtures,
  RoleFixtures,
  TicketFixtures,
  TicketServiceFixtures
}
import munit.FunSuite

import java.util.UUID

class TicketServiceLiveSpec extends FunSuite {

  test("createTicket persists a ticket when the actor can create tickets") {
    val ticket = TicketFixtures.sampleTicket

    val result = TicketServiceFixtures.withTicketService(
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminTicketPermission)
    ) { ctx =>
      for {
        created <- ctx.ticketService.createTicket(ticket, BoardMemberFixtures.sampleMember.userId)
        stored  <- ctx.ticketRepo.findById(ticket.id)
      } yield (created, stored.map(_.createdByUserId), stored.nonEmpty)
    }

    assertEquals(
      result,
      (true, Some(BoardMemberFixtures.sampleMember.userId), true)
    )
  }

  test("createTicket returns false when the actor lacks ticket create permission") {
    val ticket = TicketFixtures.sampleTicket

    val result = TicketServiceFixtures.withTicketService(
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.viewerRole.id)),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerTicketPermission)
    ) { ctx =>
      for {
        created <- ctx.ticketService.createTicket(ticket, BoardMemberFixtures.sampleMember.userId)
        stored  <- ctx.ticketRepo.findById(ticket.id)
      } yield (created, stored)
    }

    assertEquals(result, (false, None))
  }

  test("modifyTicket updates mutable ticket fields when the actor can modify tickets") {
    val existing = TicketFixtures.sampleTicket
    val updated  =
      existing.copy(
        name = TicketName("Updated title"),
        description = None,
        commentsEnabled = false,
        originalEstimatedMinutes = Some(240)
      )

    val result = TicketServiceFixtures.withTicketService(
      tickets = List(existing),
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminTicketPermission)
    ) { ctx =>
      for {
        modified <- ctx.ticketService.modifyTicket(updated, BoardMemberFixtures.sampleMember.userId)
        stored   <- ctx.ticketRepo.findById(existing.id)
      } yield (modified, stored.map(_.name), stored.map(_.commentsEnabled))
    }

    assertEquals(result, (true, Some(TicketName("Updated title")), Some(false)))
  }

  test("modifyTicket returns false when the actor lacks ticket modify permission") {
    val existing = TicketFixtures.sampleTicket
    val updated  = existing.copy(name = TicketName("Blocked update"))

    val result = TicketServiceFixtures.withTicketService(
      tickets = List(existing),
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.viewerRole.id)),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerTicketPermission)
    ) { ctx =>
      for {
        modified <- ctx.ticketService.modifyTicket(updated, BoardMemberFixtures.sampleMember.userId)
        stored   <- ctx.ticketRepo.findById(existing.id)
      } yield (modified, stored.map(_.name))
    }

    assertEquals(result, (false, Some(existing.name)))
  }

  test("reassignTicket updates assignee when the actor can reassign tickets") {
    val existing    = TicketFixtures.sampleTicket
    val newAssignee = UserId(UUID.fromString("33333333-3333-3333-3333-333333333333"))

    val result = TicketServiceFixtures.withTicketService(
      tickets = List(existing),
      dashboards = List(BoardFixtures.sampleDashboard),
      members =
        List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.contributorRole.id)),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorTicketPermission)
    ) { ctx =>
      for {
        reassigned <- ctx.ticketService.reassignTicket(
                        existing.id,
                        BoardMemberFixtures.sampleMember.userId,
                        Some(newAssignee)
                      )
        stored     <- ctx.ticketRepo.findById(existing.id)
      } yield (reassigned, stored.flatMap(_.assignedToUserId))
    }

    assertEquals(result, (true, Some(newAssignee)))
  }

  test("deleteTicket removes the ticket when the actor can delete tickets") {
    val existing = TicketFixtures.sampleTicket

    val result = TicketServiceFixtures.withTicketService(
      tickets = List(existing),
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminTicketPermission)
    ) { ctx =>
      for {
        deleted <-
          ctx.ticketService.deleteTicket(existing.id, BoardMemberFixtures.sampleMember.userId)
        stored  <- ctx.ticketRepo.findById(existing.id)
      } yield (deleted, stored)
    }

    assertEquals(result, (true, None))
  }

  test("deleteTicket returns false when the actor lacks ticket delete permission") {
    val existing = TicketFixtures.sampleTicket

    val result = TicketServiceFixtures.withTicketService(
      tickets = List(existing),
      dashboards = List(BoardFixtures.sampleDashboard),
      members =
        List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.contributorRole.id)),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorTicketPermission)
    ) { ctx =>
      for {
        deleted <-
          ctx.ticketService.deleteTicket(existing.id, BoardMemberFixtures.sampleMember.userId)
        stored  <- ctx.ticketRepo.findById(existing.id)
      } yield (deleted, stored.nonEmpty)
    }

    assertEquals(result, (false, true))
  }
}
