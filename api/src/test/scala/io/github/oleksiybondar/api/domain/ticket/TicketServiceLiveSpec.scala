package io.github.oleksiybondar.api.domain.ticket

import cats.effect.IO
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
    val command = CreateTicketCommand(
      boardId = TicketFixtures.sampleTicket.boardId,
      name = TicketFixtures.sampleTicket.name,
      description = TicketFixtures.sampleTicket.description,
      component = TicketFixtures.sampleTicket.component,
      scope = TicketFixtures.sampleTicket.scope,
      acceptanceCriteria = TicketFixtures.sampleTicket.acceptanceCriteria,
      assignedToUserId = TicketFixtures.sampleTicket.assignedToUserId,
      originalEstimatedMinutes = TicketFixtures.sampleTicket.originalEstimatedMinutes,
      priority = TicketFixtures.sampleTicket.priority,
      severityId = TicketFixtures.sampleTicket.severityId,
      stateId = TicketFixtures.sampleTicket.stateId
    )

    val result = TicketServiceFixtures.withTicketService(
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(
        BoardMemberFixtures.sampleMember,
        BoardMemberFixtures.member(
          userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
          roleId = RoleFixtures.contributorRole.id
        )
      ),
      roles = List(RoleFixtures.adminRole, RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.adminTicketPermission)
    ) { ctx =>
      for {
        created <- ctx.ticketService.createTicket(command, BoardMemberFixtures.sampleMember.userId)
        stored  <- created match {
                     case Some(ticket) => ctx.ticketRepo.findById(ticket.id)
                     case None         => IO.pure(None)
                   }
      } yield (created.nonEmpty, stored.map(_.createdByUserId), stored.nonEmpty)
    }

    assertEquals(
      result,
      (true, Some(BoardMemberFixtures.sampleMember.userId), true)
    )
  }

  test("createTicket returns false when the actor lacks ticket create permission") {
    val command = CreateTicketCommand(
      boardId = TicketFixtures.sampleTicket.boardId,
      name = TicketFixtures.sampleTicket.name,
      stateId = TicketFixtures.sampleTicket.stateId
    )

    val result = TicketServiceFixtures.withTicketService(
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.viewerRole.id)),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerTicketPermission)
    ) { ctx =>
      for {
        created <- ctx.ticketService.createTicket(command, BoardMemberFixtures.sampleMember.userId)
      } yield created
    }

    assertEquals(result, None)
  }

  test("changeTitle updates the ticket title when the actor can modify tickets") {
    val existing = TicketFixtures.sampleTicket

    val result = TicketServiceFixtures.withTicketService(
      tickets = List(existing),
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminTicketPermission)
    ) { ctx =>
      for {
        modified <- ctx.ticketService.changeTitle(
                      existing.id,
                      BoardMemberFixtures.sampleMember.userId,
                      TicketName("Updated title")
                    )
        stored   <- ctx.ticketRepo.findById(existing.id)
      } yield (modified, stored.map(_.name))
    }

    assertEquals(result, (true, Some(TicketName("Updated title"))))
  }

  test("changeDescription updates the ticket description when the actor can modify tickets") {
    val existing = TicketFixtures.sampleTicket

    val result = TicketServiceFixtures.withTicketService(
      tickets = List(existing),
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminTicketPermission)
    ) { ctx =>
      for {
        modified <- ctx.ticketService.changeDescription(
                      existing.id,
                      BoardMemberFixtures.sampleMember.userId,
                      Some(TicketDescription("Updated description"))
                    )
        stored   <- ctx.ticketRepo.findById(existing.id)
      } yield (modified, stored.map(_.description))
    }

    assertEquals(result, (true, Some(Some(TicketDescription("Updated description")))))
  }

  test("changeAcceptanceCriteria updates the field when the actor can modify tickets") {
    val existing = TicketFixtures.sampleTicket

    val result = TicketServiceFixtures.withTicketService(
      tickets = List(existing),
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminTicketPermission)
    ) { ctx =>
      for {
        modified <- ctx.ticketService.changeAcceptanceCriteria(
                      existing.id,
                      BoardMemberFixtures.sampleMember.userId,
                      Some(TicketAcceptanceCriteria("New acceptance criteria"))
                    )
        stored   <- ctx.ticketRepo.findById(existing.id)
      } yield (modified, stored.map(_.acceptanceCriteria))
    }

    assertEquals(
      result,
      (true, Some(Some(TicketAcceptanceCriteria("New acceptance criteria"))))
    )
  }

  test("changeEstimatedTime updates minutes when the actor can modify tickets") {
    val existing = TicketFixtures.sampleTicket

    val result = TicketServiceFixtures.withTicketService(
      tickets = List(existing),
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminTicketPermission)
    ) { ctx =>
      for {
        modified <- ctx.ticketService.changeEstimatedTime(
                      existing.id,
                      BoardMemberFixtures.sampleMember.userId,
                      Some(240)
                    )
        stored   <- ctx.ticketRepo.findById(existing.id)
      } yield (modified, stored.map(_.originalEstimatedMinutes))
    }

    assertEquals(result, (true, Some(Some(240))))
  }

  test("changeTitle returns false when the actor lacks ticket modify permission") {
    val existing = TicketFixtures.sampleTicket

    val result = TicketServiceFixtures.withTicketService(
      tickets = List(existing),
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.viewerRole.id)),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerTicketPermission)
    ) { ctx =>
      for {
        modified <- ctx.ticketService.changeTitle(
                      existing.id,
                      BoardMemberFixtures.sampleMember.userId,
                      TicketName("Blocked update")
                    )
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
      members = List(
        BoardMemberFixtures.sampleMember.copy(roleId = RoleFixtures.contributorRole.id),
        BoardMemberFixtures.member(
          userId = newAssignee,
          roleId = RoleFixtures.viewerRole.id
        )
      ),
      roles = List(RoleFixtures.contributorRole, RoleFixtures.viewerRole),
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

  test("reassignTicket returns false when the target user is not a board member") {
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

    assertEquals(result, (false, existing.assignedToUserId))
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
