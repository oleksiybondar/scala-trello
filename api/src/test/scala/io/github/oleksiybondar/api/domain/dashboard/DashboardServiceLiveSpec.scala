package io.github.oleksiybondar.api.domain.dashboard

import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.{
  DashboardFixtures,
  DashboardMemberFixtures,
  DashboardServiceFixtures,
  PermissionFixtures,
  RoleFixtures
}
import munit.FunSuite

import java.util.UUID

class DashboardServiceLiveSpec extends FunSuite {

  test("createDashboard persists the dashboard and adds the owner as admin member") {
    val dashboard = DashboardFixtures.sampleDashboard

    val result = DashboardServiceFixtures.withDashboardService(
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        _               <- ctx.dashboardService.createDashboard(dashboard)
        storedDashboard <- ctx.dashboardRepo.findById(dashboard.id)
        storedMember    <-
          ctx.dashboardMembershipService.findMember(dashboard.id, dashboard.ownerUserId)
      } yield (storedDashboard, storedMember.map(_.member.roleId))
    }

    assertEquals(result, (Some(dashboard), Some(RoleId(1))))
  }

  test("changeOwnership updates owner and ensures the new owner is admin member") {
    val dashboard          = DashboardFixtures.sampleDashboard
    val currentOwnerMember = DashboardMemberFixtures.sampleMember
    val newOwnerUserId     = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
    val newOwnerMember     =
      DashboardMemberFixtures.member(
        userId = newOwnerUserId,
        roleId = RoleId(3)
      )

    val result = DashboardServiceFixtures.withDashboardService(
      dashboards = List(dashboard),
      members = List(currentOwnerMember, newOwnerMember),
      roles = List(RoleFixtures.adminRole, RoleFixtures.viewerRole),
      permissions = List(
        PermissionFixtures.adminDashboardPermission,
        PermissionFixtures.viewerDashboardPermission
      )
    ) { ctx =>
      for {
        changed       <-
          ctx.dashboardService.changeOwnership(dashboard.id, dashboard.ownerUserId, newOwnerUserId)
        updatedBoard  <- ctx.dashboardRepo.findById(dashboard.id)
        updatedMember <- ctx.dashboardMembershipService.findMember(dashboard.id, newOwnerUserId)
      } yield (changed, updatedBoard.map(_.ownerUserId), updatedMember.map(_.member.roleId))
    }

    assertEquals(result, (true, Some(newOwnerUserId), Some(RoleId(1))))
  }

  test("listDashboardsForUser returns dashboards where the user is a member") {
    val firstDashboard  = DashboardFixtures.sampleDashboard
    val secondDashboard =
      DashboardFixtures.dashboard(
        id = DashboardId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")),
        ownerUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        createdByUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        lastModifiedByUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
      )
    val firstMember     = DashboardMemberFixtures.sampleMember
    val secondMember    =
      DashboardMemberFixtures.member(
        dashboardId = secondDashboard.id,
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
      )

    val result = DashboardServiceFixtures.withDashboardService(
      dashboards = List(firstDashboard, secondDashboard),
      members = List(firstMember, secondMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardService.listDashboardsForUser(firstMember.userId)
    }

    assertEquals(result, List(firstDashboard))
  }

  test("deactivate marks the dashboard inactive when the actor has access") {
    val dashboard = DashboardFixtures.sampleDashboard

    val result = DashboardServiceFixtures.withDashboardService(
      dashboards = List(dashboard),
      members = List(DashboardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        deactivated <- ctx.dashboardService.deactivate(dashboard.id, dashboard.ownerUserId)
        updated     <- ctx.dashboardRepo.findById(dashboard.id)
      } yield (deactivated, updated.map(_.active))
    }

    assertEquals(result, (true, Some(false)))
  }

  test("addMember adds a membership when the actor has access and the role exists") {
    val dashboard    = DashboardFixtures.sampleDashboard
    val memberUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))

    val result = DashboardServiceFixtures.withDashboardService(
      dashboards = List(dashboard),
      members = List(DashboardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole, RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        added  <- ctx.dashboardService.addMember(
                    dashboard.id,
                    dashboard.ownerUserId,
                    memberUserId,
                    RoleId(3)
                  )
        member <- ctx.dashboardMembershipService.findMember(dashboard.id, memberUserId)
      } yield (added, member.map(_.member.roleId))
    }

    assertEquals(result, (true, Some(RoleId(3))))
  }

  test("removeMember removes a membership when the actor has access") {
    val dashboard      = DashboardFixtures.sampleDashboard
    val memberToRemove =
      DashboardMemberFixtures.member(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = RoleId(3)
      )

    val result = DashboardServiceFixtures.withDashboardService(
      dashboards = List(dashboard),
      members = List(DashboardMemberFixtures.sampleMember, memberToRemove),
      roles = List(RoleFixtures.adminRole, RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        removed <- ctx.dashboardService.removeMember(
                     dashboard.id,
                     dashboard.ownerUserId,
                     memberToRemove.userId
                   )
        member  <- ctx.dashboardMembershipService.findMember(dashboard.id, memberToRemove.userId)
      } yield (removed, member)
    }

    assertEquals(result, (true, None))
  }
}
