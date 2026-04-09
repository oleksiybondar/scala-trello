package io.github.oleksiybondar.api.domain.board

import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.{
  BoardFixtures,
  BoardMemberFixtures,
  BoardServiceFixtures,
  PermissionFixtures,
  RoleFixtures
}
import munit.FunSuite

import java.util.UUID

class BoardServiceLiveSpec extends FunSuite {

  test("createDashboard persists the dashboard and adds the owner as admin member") {
    val dashboard = BoardFixtures.sampleDashboard

    val result = BoardServiceFixtures.withBoardService(
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
    val dashboard          = BoardFixtures.sampleDashboard
    val currentOwnerMember = BoardMemberFixtures.sampleMember
    val newOwnerUserId     = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
    val newOwnerMember     =
      BoardMemberFixtures.member(
        userId = newOwnerUserId,
        roleId = RoleId(3)
      )

    val result = BoardServiceFixtures.withBoardService(
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
    val firstDashboard  = BoardFixtures.sampleDashboard
    val secondDashboard =
      BoardFixtures.dashboard(
        id = BoardId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")),
        ownerUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        createdByUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        lastModifiedByUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
      )
    val firstMember     = BoardMemberFixtures.sampleMember
    val secondMember    =
      BoardMemberFixtures.member(
        boardId = secondDashboard.id,
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
      )

    val result = BoardServiceFixtures.withBoardService(
      dashboards = List(firstDashboard, secondDashboard),
      members = List(firstMember, secondMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardService.listDashboardsForUser(firstMember.userId)
    }

    assertEquals(result, List(firstDashboard))
  }

  test("listDashboardsForUser filters dashboards by keyword and owner") {
    val matchingDashboard   =
      BoardFixtures.sampleDashboard.copy(name = BoardName("Platform Sprint"))
    val otherOwnedDashboard =
      BoardFixtures.dashboard(
        id = BoardId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")),
        name = BoardName("Infra Board"),
        ownerUserId = matchingDashboard.ownerUserId,
        createdByUserId = matchingDashboard.ownerUserId,
        lastModifiedByUserId = matchingDashboard.ownerUserId
      )
    val otherOwnerDashboard =
      BoardFixtures.dashboard(
        id = BoardId(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")),
        name = BoardName("Platform Ops"),
        ownerUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        createdByUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        lastModifiedByUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
      )
    val memberships         = List(
      BoardMemberFixtures.sampleMember.copy(boardId = matchingDashboard.id),
      BoardMemberFixtures.sampleMember.copy(boardId = otherOwnedDashboard.id),
      BoardMemberFixtures.sampleMember.copy(boardId = otherOwnerDashboard.id)
    )

    val result = BoardServiceFixtures.withBoardService(
      dashboards = List(matchingDashboard, otherOwnedDashboard, otherOwnerDashboard),
      members = memberships,
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardService.listDashboardsForUser(
        BoardMemberFixtures.sampleMember.userId,
        BoardQueryFilters(
          keyword = Some("platform"),
          ownerUserId = Some(matchingDashboard.ownerUserId)
        )
      )
    }

    assertEquals(result, List(matchingDashboard))
  }

  test("listDashboardsForUser can include inactive dashboards when active filter is unset") {
    val activeBoard   = BoardFixtures.sampleDashboard
    val inactiveBoard =
      BoardFixtures.dashboard(
        id = BoardId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")),
        name = BoardName("Archived Board"),
        active = false
      )
    val memberships   = List(
      BoardMemberFixtures.sampleMember.copy(boardId = activeBoard.id),
      BoardMemberFixtures.sampleMember.copy(boardId = inactiveBoard.id)
    )

    val result = BoardServiceFixtures.withBoardService(
      dashboards = List(activeBoard, inactiveBoard),
      members = memberships,
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      ctx.dashboardService.listDashboardsForUser(
        BoardMemberFixtures.sampleMember.userId,
        BoardQueryFilters(active = None)
      )
    }

    assertEquals(result.map(_.id), List(activeBoard.id, inactiveBoard.id))
  }

  test("deactivate marks the dashboard inactive when the actor has access") {
    val dashboard = BoardFixtures.sampleDashboard

    val result = BoardServiceFixtures.withBoardService(
      dashboards = List(dashboard),
      members = List(BoardMemberFixtures.sampleMember),
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

  test("activate marks the dashboard active when the actor has access") {
    val dashboard = BoardFixtures.sampleDashboard.copy(active = false)

    val result = BoardServiceFixtures.withBoardService(
      dashboards = List(dashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        activated <- ctx.dashboardService.activate(dashboard.id, dashboard.ownerUserId)
        updated   <- ctx.dashboardRepo.findById(dashboard.id)
      } yield (activated, updated.map(_.active))
    }

    assertEquals(result, (true, Some(true)))
  }

  test("changeTitle updates the dashboard name when the actor has access") {
    val dashboard = BoardFixtures.sampleDashboard

    val result = BoardServiceFixtures.withBoardService(
      dashboards = List(dashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        changed <- ctx.dashboardService.changeTitle(
                     dashboard.id,
                     dashboard.ownerUserId,
                     BoardName("Platform Board")
                   )
        updated <- ctx.dashboardRepo.findById(dashboard.id)
      } yield (changed, updated.map(_.name), updated.map(_.lastModifiedByUserId))
    }

    assertEquals(
      result,
      (true, Some(BoardName("Platform Board")), Some(dashboard.ownerUserId))
    )
  }

  test("changeDescription updates the dashboard description when the actor has access") {
    val dashboard = BoardFixtures.sampleDashboard

    val result = BoardServiceFixtures.withBoardService(
      dashboards = List(dashboard),
      members = List(BoardMemberFixtures.sampleMember),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        changed <- ctx.dashboardService.changeDescription(
                     dashboard.id,
                     dashboard.ownerUserId,
                     Some(BoardDescription("Updated board description"))
                   )
        updated <- ctx.dashboardRepo.findById(dashboard.id)
      } yield (changed, updated.flatMap(_.description).map(_.value))
    }

    assertEquals(result, (true, Some("Updated board description")))
  }

  test("addMember adds a membership when the actor has access and the role exists") {
    val dashboard    = BoardFixtures.sampleDashboard
    val memberUserId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))

    val result = BoardServiceFixtures.withBoardService(
      dashboards = List(dashboard),
      members = List(BoardMemberFixtures.sampleMember),
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

  test("addMember returns false when the invited user is already a dashboard member") {
    val dashboard      = BoardFixtures.sampleDashboard
    val existingMember =
      BoardMemberFixtures.member(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = RoleId(3)
      )

    val result = BoardServiceFixtures.withBoardService(
      dashboards = List(dashboard),
      members = List(BoardMemberFixtures.sampleMember, existingMember),
      roles = List(RoleFixtures.adminRole, RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        added  <- ctx.dashboardService.addMember(
                    dashboard.id,
                    dashboard.ownerUserId,
                    existingMember.userId,
                    RoleId(1)
                  )
        member <- ctx.dashboardMembershipService.findMember(dashboard.id, existingMember.userId)
      } yield (added, member.map(_.member.roleId))
    }

    assertEquals(result, (false, Some(RoleId(3))))
  }

  test("changeMemberRole updates a membership role when the actor has access and the role exists") {
    val dashboard = BoardFixtures.sampleDashboard
    val member    =
      BoardMemberFixtures.member(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = RoleId(3)
      )

    val result = BoardServiceFixtures.withBoardService(
      dashboards = List(dashboard),
      members = List(BoardMemberFixtures.sampleMember, member),
      roles = List(RoleFixtures.adminRole, RoleFixtures.viewerRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        changed <- ctx.dashboardService.changeMemberRole(
                     dashboard.id,
                     dashboard.ownerUserId,
                     member.userId,
                     RoleId(1)
                   )
        updated <- ctx.dashboardMembershipService.findMember(dashboard.id, member.userId)
      } yield (changed, updated.map(_.member.roleId))
    }

    assertEquals(result, (true, Some(RoleId(1))))
  }

  test("removeMember removes a membership when the actor has access") {
    val dashboard      = BoardFixtures.sampleDashboard
    val memberToRemove =
      BoardMemberFixtures.member(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = RoleId(3)
      )

    val result = BoardServiceFixtures.withBoardService(
      dashboards = List(dashboard),
      members = List(BoardMemberFixtures.sampleMember, memberToRemove),
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
