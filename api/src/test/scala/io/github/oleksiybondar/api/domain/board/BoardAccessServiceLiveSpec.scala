package io.github.oleksiybondar.api.domain.board

import io.github.oleksiybondar.api.domain.permission.PermissionArea
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.{
  BoardAccessServiceFixtures,
  BoardFixtures,
  BoardMemberFixtures,
  PermissionFixtures,
  RoleFixtures
}
import munit.FunSuite

import java.util.UUID

class BoardAccessServiceLiveSpec extends FunSuite {

  test("canRead returns true when the member role allows the requested area") {
    val result = BoardAccessServiceFixtures.withBoardAccessService(
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canRead(
        BoardMemberFixtures.sampleMember.boardId,
        BoardMemberFixtures.sampleMember.userId,
        PermissionArea.Board
      )
    }

    assertEquals(result, true)
  }

  test("canDelete returns false when the member role denies the requested area") {
    val viewerMember = BoardMemberFixtures.sampleMember.copy(roleId =
      io.github.oleksiybondar.api.domain.permission.RoleId(3)
    )

    val result = BoardAccessServiceFixtures.withBoardAccessService(
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(viewerMember),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canDelete(
        viewerMember.boardId,
        viewerMember.userId,
        PermissionArea.Board
      )
    }

    assertEquals(result, false)
  }

  test("canModify returns false when the user is not a dashboard member") {
    val result = BoardAccessServiceFixtures.withBoardAccessService(
      dashboards = List(BoardFixtures.sampleDashboard),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canModify(
        BoardMemberFixtures.sampleMember.boardId,
        UserId(UUID.fromString("99999999-9999-9999-9999-999999999999")),
        PermissionArea.Board
      )
    }

    assertEquals(result, false)
  }

  test("dashboard convenience methods delegate to dashboard area checks") {
    val result = BoardAccessServiceFixtures.withBoardAccessService(
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        canRead     <- ctx.dashboardAccessService.canReadDashboard(
                         BoardMemberFixtures.sampleMember.boardId,
                         BoardMemberFixtures.sampleMember.userId
                       )
        canModify   <- ctx.dashboardAccessService.canModifyDashboard(
                         BoardMemberFixtures.sampleMember.boardId,
                         BoardMemberFixtures.sampleMember.userId
                       )
        canReassign <- ctx.dashboardAccessService.canReassignDashboard(
                         BoardMemberFixtures.sampleMember.boardId,
                         BoardMemberFixtures.sampleMember.userId
                       )
      } yield (canRead, canModify, canReassign)
    }

    assertEquals(result, (true, true, true))
  }

  test("ticket convenience methods delegate to ticket area checks") {
    val contributorMember =
      BoardMemberFixtures.sampleMember.copy(roleId =
        io.github.oleksiybondar.api.domain.permission.RoleId(2)
      )

    val result = BoardAccessServiceFixtures.withBoardAccessService(
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(contributorMember),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorTicketPermission)
    ) { ctx =>
      for {
        canRead     <- ctx.dashboardAccessService.canReadTicket(
                         contributorMember.boardId,
                         contributorMember.userId
                       )
        canCreate   <- ctx.dashboardAccessService.canCreateTicket(
                         contributorMember.boardId,
                         contributorMember.userId
                       )
        canReassign <- ctx.dashboardAccessService.canReassignTicket(
                         contributorMember.boardId,
                         contributorMember.userId
                       )
      } yield (canRead, canCreate, canReassign)
    }

    assertEquals(result, (true, true, true))
  }

  test("comment convenience methods delegate to comment area checks") {
    val contributorMember =
      BoardMemberFixtures.sampleMember.copy(roleId =
        io.github.oleksiybondar.api.domain.permission.RoleId(2)
      )

    val result = BoardAccessServiceFixtures.withBoardAccessService(
      dashboards = List(BoardFixtures.sampleDashboard),
      members = List(contributorMember),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorCommentPermission)
    ) { ctx =>
      for {
        canRead   <- ctx.dashboardAccessService.canReadComment(
                       contributorMember.boardId,
                       contributorMember.userId
                     )
        canModify <- ctx.dashboardAccessService.canModifyComment(
                       contributorMember.boardId,
                       contributorMember.userId
                     )
        canDelete <- ctx.dashboardAccessService.canDeleteComment(
                       contributorMember.boardId,
                       contributorMember.userId
                     )
      } yield (canRead, canModify, canDelete)
    }

    assertEquals(result, (true, true, false))
  }

  test("returns false when the dashboard does not exist") {
    val result = BoardAccessServiceFixtures.withBoardAccessService(
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canReadDashboard(
        BoardMemberFixtures.sampleMember.boardId,
        BoardMemberFixtures.sampleMember.userId
      )
    }

    assertEquals(result, false)
  }

  test("allows read access for inactive dashboards when the requester is a member") {
    val inactiveDashboard = BoardFixtures.sampleDashboard.copy(active = false)

    val viewerMember =
      BoardMemberFixtures.sampleMember.copy(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = io.github.oleksiybondar.api.domain.permission.RoleId(3)
      )

    val result = BoardAccessServiceFixtures.withBoardAccessService(
      dashboards = List(inactiveDashboard),
      members = List(viewerMember),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canReadDashboard(viewerMember.boardId, viewerMember.userId)
    }

    assertEquals(result, true)
  }

  test("returns false for inactive dashboards modify access when the requester is not the owner") {
    val inactiveDashboard = BoardFixtures.sampleDashboard.copy(active = false)

    val viewerMember =
      BoardMemberFixtures.sampleMember.copy(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = io.github.oleksiybondar.api.domain.permission.RoleId(3)
      )

    val result = BoardAccessServiceFixtures.withBoardAccessService(
      dashboards = List(inactiveDashboard),
      members = List(viewerMember),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canModifyDashboard(viewerMember.boardId, viewerMember.userId)
    }

    assertEquals(result, false)
  }

  test("inactive dashboards still resolve permissions for the owner") {
    val inactiveDashboard = BoardFixtures.sampleDashboard.copy(active = false)

    val result = BoardAccessServiceFixtures.withBoardAccessService(
      dashboards = List(inactiveDashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canReadDashboard(
        BoardMemberFixtures.sampleMember.boardId,
        BoardMemberFixtures.sampleMember.userId
      )
    }

    assertEquals(result, true)
  }
}
