package io.github.oleksiybondar.api.domain.dashboard

import io.github.oleksiybondar.api.domain.permission.{RoleId, RoleWithPermissions}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.{
  DashboardMemberFixtures,
  DashboardMembershipServiceFixtures,
  PermissionFixtures,
  RoleFixtures
}
import munit.FunSuite

import java.time.Instant
import java.util.UUID

class DashboardMembershipServiceLiveSpec extends FunSuite {

  test("addMember persists a membership") {
    val member = DashboardMemberFixtures.sampleMember

    val result = DashboardMembershipServiceFixtures.withDashboardMembershipService(
      roles = List(RoleFixtures.adminRole),
      permissions = List(
        PermissionFixtures.adminDashboardPermission,
        PermissionFixtures.adminTicketPermission,
        PermissionFixtures.adminCommentPermission
      )
    ) { ctx =>
      for {
        _      <- ctx.dashboardMembershipService.addMember(member)
        result <- ctx.dashboardMembershipService.findMember(member.dashboardId, member.userId)
      } yield result
    }

    assertEquals(
      result,
      Some(
        DashboardMemberWithRole(
          member,
          RoleWithPermissions(
            RoleFixtures.adminRole,
            List(
              PermissionFixtures.adminDashboardPermission,
              PermissionFixtures.adminTicketPermission,
              PermissionFixtures.adminCommentPermission
            )
          )
        )
      )
    )
  }

  test("removeMember deletes an existing membership") {
    val member = DashboardMemberFixtures.sampleMember

    val result = DashboardMembershipServiceFixtures.withDashboardMembershipService(
      members = List(member),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        removed <- ctx.dashboardMembershipService.removeMember(member.dashboardId, member.userId)
        result  <- ctx.dashboardMembershipService.findMember(member.dashboardId, member.userId)
      } yield (removed, result)
    }

    assertEquals(result, (true, None))
  }

  test("changeMemberRole updates the role for an existing membership") {
    val member = DashboardMemberFixtures.sampleMember

    val result = DashboardMembershipServiceFixtures.withDashboardMembershipService(
      members = List(member),
      roles = List(RoleFixtures.adminRole, RoleFixtures.viewerRole),
      permissions = List(
        PermissionFixtures.adminDashboardPermission,
        PermissionFixtures.viewerDashboardPermission
      )
    ) { ctx =>
      for {
        updated <- ctx.dashboardMembershipService.changeMemberRole(
                     member.dashboardId,
                     member.userId,
                     RoleId(3)
                   )
        result  <- ctx.dashboardMembershipService.findMember(member.dashboardId, member.userId)
      } yield (updated, result)
    }

    result match {
      case (updated, Some(memberWithRole)) =>
        assertEquals(updated, true)
        assertEquals(memberWithRole.member.roleId, RoleId(3))
        assertEquals(memberWithRole.role.role, RoleFixtures.viewerRole)
      case other                           => fail(s"Unexpected result: $other")
    }
  }

  test("findMember returns the membership with the assembled role") {
    val member = DashboardMemberFixtures.sampleMember

    val result = DashboardMembershipServiceFixtures.withDashboardMembershipService(
      members = List(member),
      roles = List(RoleFixtures.adminRole),
      permissions = List(
        PermissionFixtures.adminDashboardPermission,
        PermissionFixtures.adminTicketPermission,
        PermissionFixtures.adminCommentPermission
      )
    ) { ctx =>
      ctx.dashboardMembershipService.findMember(member.dashboardId, member.userId)
    }

    assertEquals(
      result,
      Some(
        DashboardMemberWithRole(
          member,
          RoleWithPermissions(
            RoleFixtures.adminRole,
            List(
              PermissionFixtures.adminDashboardPermission,
              PermissionFixtures.adminTicketPermission,
              PermissionFixtures.adminCommentPermission
            )
          )
        )
      )
    )
  }

  test("listMembers returns dashboard memberships with assembled roles") {
    val firstMember  = DashboardMemberFixtures.sampleMember
    val secondMember =
      DashboardMemberFixtures.member(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = RoleId(3),
        createdAt = Instant.parse("2026-04-06T08:05:00Z")
      )

    val result = DashboardMembershipServiceFixtures.withDashboardMembershipService(
      members = List(firstMember, secondMember),
      roles = List(RoleFixtures.adminRole, RoleFixtures.viewerRole),
      permissions = List(
        PermissionFixtures.adminDashboardPermission,
        PermissionFixtures.adminTicketPermission,
        PermissionFixtures.adminCommentPermission,
        PermissionFixtures.viewerDashboardPermission,
        PermissionFixtures.viewerTicketPermission,
        PermissionFixtures.viewerCommentPermission
      )
    ) { ctx =>
      ctx.dashboardMembershipService.listMembers(firstMember.dashboardId)
    }

    assertEquals(
      result,
      List(
        DashboardMemberWithRole(
          firstMember,
          RoleWithPermissions(
            RoleFixtures.adminRole,
            List(
              PermissionFixtures.adminDashboardPermission,
              PermissionFixtures.adminTicketPermission,
              PermissionFixtures.adminCommentPermission
            )
          )
        ),
        DashboardMemberWithRole(
          secondMember,
          RoleWithPermissions(
            RoleFixtures.viewerRole,
            List(
              PermissionFixtures.viewerDashboardPermission,
              PermissionFixtures.viewerTicketPermission,
              PermissionFixtures.viewerCommentPermission
            )
          )
        )
      )
    )
  }

  test("listMembershipsForUser returns memberships for the requested user with assembled roles") {
    val firstMember  = DashboardMemberFixtures.sampleMember
    val secondMember =
      DashboardMemberFixtures.member(
        dashboardId = DashboardId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")),
        createdAt = Instant.parse("2026-04-06T08:05:00Z")
      )
    val otherMember  =
      DashboardMemberFixtures.member(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = RoleId(3)
      )

    val result = DashboardMembershipServiceFixtures.withDashboardMembershipService(
      members = List(firstMember, secondMember, otherMember),
      roles = List(RoleFixtures.adminRole, RoleFixtures.viewerRole),
      permissions = List(
        PermissionFixtures.adminDashboardPermission,
        PermissionFixtures.adminTicketPermission,
        PermissionFixtures.adminCommentPermission,
        PermissionFixtures.viewerDashboardPermission,
        PermissionFixtures.viewerTicketPermission,
        PermissionFixtures.viewerCommentPermission
      )
    ) { ctx =>
      ctx.dashboardMembershipService.listMembershipsForUser(firstMember.userId)
    }

    assertEquals(
      result,
      List(
        DashboardMemberWithRole(
          firstMember,
          RoleWithPermissions(
            RoleFixtures.adminRole,
            List(
              PermissionFixtures.adminDashboardPermission,
              PermissionFixtures.adminTicketPermission,
              PermissionFixtures.adminCommentPermission
            )
          )
        ),
        DashboardMemberWithRole(
          secondMember,
          RoleWithPermissions(
            RoleFixtures.adminRole,
            List(
              PermissionFixtures.adminDashboardPermission,
              PermissionFixtures.adminTicketPermission,
              PermissionFixtures.adminCommentPermission
            )
          )
        )
      )
    )
  }
}
