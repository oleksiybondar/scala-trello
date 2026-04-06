package io.github.oleksiybondar.api.domain.dashboard

import io.github.oleksiybondar.api.domain.permission.PermissionArea
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.{
  DashboardAccessServiceFixtures,
  DashboardFixtures,
  DashboardMemberFixtures,
  PermissionFixtures,
  RoleFixtures
}
import munit.FunSuite

import java.util.UUID

class DashboardAccessServiceLiveSpec extends FunSuite {

  test("canRead returns true when the member role allows the requested area") {
    val result = DashboardAccessServiceFixtures.withDashboardAccessService(
      dashboards = List(DashboardFixtures.sampleDashboard),
      members = List(DashboardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canRead(
        DashboardMemberFixtures.sampleMember.dashboardId,
        DashboardMemberFixtures.sampleMember.userId,
        PermissionArea.Dashboard
      )
    }

    assertEquals(result, true)
  }

  test("canDelete returns false when the member role denies the requested area") {
    val viewerMember = DashboardMemberFixtures.sampleMember.copy(roleId =
      io.github.oleksiybondar.api.domain.permission.RoleId(3)
    )

    val result = DashboardAccessServiceFixtures.withDashboardAccessService(
      dashboards = List(DashboardFixtures.sampleDashboard),
      members = List(viewerMember),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canDelete(
        viewerMember.dashboardId,
        viewerMember.userId,
        PermissionArea.Dashboard
      )
    }

    assertEquals(result, false)
  }

  test("canModify returns false when the user is not a dashboard member") {
    val result = DashboardAccessServiceFixtures.withDashboardAccessService(
      dashboards = List(DashboardFixtures.sampleDashboard),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canModify(
        DashboardMemberFixtures.sampleMember.dashboardId,
        UserId(UUID.fromString("99999999-9999-9999-9999-999999999999")),
        PermissionArea.Dashboard
      )
    }

    assertEquals(result, false)
  }

  test("dashboard convenience methods delegate to dashboard area checks") {
    val result = DashboardAccessServiceFixtures.withDashboardAccessService(
      dashboards = List(DashboardFixtures.sampleDashboard),
      members = List(DashboardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        canRead     <- ctx.dashboardAccessService.canReadDashboard(
                         DashboardMemberFixtures.sampleMember.dashboardId,
                         DashboardMemberFixtures.sampleMember.userId
                       )
        canModify   <- ctx.dashboardAccessService.canModifyDashboard(
                         DashboardMemberFixtures.sampleMember.dashboardId,
                         DashboardMemberFixtures.sampleMember.userId
                       )
        canReassign <- ctx.dashboardAccessService.canReassignDashboard(
                         DashboardMemberFixtures.sampleMember.dashboardId,
                         DashboardMemberFixtures.sampleMember.userId
                       )
      } yield (canRead, canModify, canReassign)
    }

    assertEquals(result, (true, true, true))
  }

  test("ticket convenience methods delegate to ticket area checks") {
    val contributorMember =
      DashboardMemberFixtures.sampleMember.copy(roleId =
        io.github.oleksiybondar.api.domain.permission.RoleId(2)
      )

    val result = DashboardAccessServiceFixtures.withDashboardAccessService(
      dashboards = List(DashboardFixtures.sampleDashboard),
      members = List(contributorMember),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorTicketPermission)
    ) { ctx =>
      for {
        canRead     <- ctx.dashboardAccessService.canReadTicket(
                         contributorMember.dashboardId,
                         contributorMember.userId
                       )
        canCreate   <- ctx.dashboardAccessService.canCreateTicket(
                         contributorMember.dashboardId,
                         contributorMember.userId
                       )
        canReassign <- ctx.dashboardAccessService.canReassignTicket(
                         contributorMember.dashboardId,
                         contributorMember.userId
                       )
      } yield (canRead, canCreate, canReassign)
    }

    assertEquals(result, (true, true, true))
  }

  test("comment convenience methods delegate to comment area checks") {
    val contributorMember =
      DashboardMemberFixtures.sampleMember.copy(roleId =
        io.github.oleksiybondar.api.domain.permission.RoleId(2)
      )

    val result = DashboardAccessServiceFixtures.withDashboardAccessService(
      dashboards = List(DashboardFixtures.sampleDashboard),
      members = List(contributorMember),
      roles = List(RoleFixtures.contributorRole),
      permissions = List(PermissionFixtures.contributorCommentPermission)
    ) { ctx =>
      for {
        canRead   <- ctx.dashboardAccessService.canReadComment(
                       contributorMember.dashboardId,
                       contributorMember.userId
                     )
        canModify <- ctx.dashboardAccessService.canModifyComment(
                       contributorMember.dashboardId,
                       contributorMember.userId
                     )
        canDelete <- ctx.dashboardAccessService.canDeleteComment(
                       contributorMember.dashboardId,
                       contributorMember.userId
                     )
      } yield (canRead, canModify, canDelete)
    }

    assertEquals(result, (true, true, false))
  }

  test("returns false when the dashboard does not exist") {
    val result = DashboardAccessServiceFixtures.withDashboardAccessService(
      members = List(DashboardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canReadDashboard(
        DashboardMemberFixtures.sampleMember.dashboardId,
        DashboardMemberFixtures.sampleMember.userId
      )
    }

    assertEquals(result, false)
  }

  test("returns false for inactive dashboards when the requester is not the owner") {
    val inactiveDashboard = DashboardFixtures.sampleDashboard.copy(active = false)

    val viewerMember =
      DashboardMemberFixtures.sampleMember.copy(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = io.github.oleksiybondar.api.domain.permission.RoleId(3)
      )

    val result = DashboardAccessServiceFixtures.withDashboardAccessService(
      dashboards = List(inactiveDashboard),
      members = List(viewerMember),
      roles = List(RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.viewerDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canReadDashboard(viewerMember.dashboardId, viewerMember.userId)
    }

    assertEquals(result, false)
  }

  test("inactive dashboards still resolve permissions for the owner") {
    val inactiveDashboard = DashboardFixtures.sampleDashboard.copy(active = false)

    val result = DashboardAccessServiceFixtures.withDashboardAccessService(
      dashboards = List(inactiveDashboard),
      members = List(DashboardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardAccessService.canReadDashboard(
        DashboardMemberFixtures.sampleMember.dashboardId,
        DashboardMemberFixtures.sampleMember.userId
      )
    }

    assertEquals(result, true)
  }
}
