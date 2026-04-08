package io.github.oleksiybondar.api.domain.board

import io.github.oleksiybondar.api.domain.permission.{RoleId, RoleWithPermissions}
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.{
  BoardMemberFixtures,
  BoardMembershipServiceFixtures,
  PermissionFixtures,
  RoleFixtures
}
import munit.FunSuite

import java.time.Instant
import java.util.UUID

class BoardMembershipServiceLiveSpec extends FunSuite {

  test("addMember persists a membership") {
    val member = BoardMemberFixtures.sampleMember

    val result = BoardMembershipServiceFixtures.withBoardMembershipService(
      roles = List(RoleFixtures.adminRole),
      permissions = List(
        PermissionFixtures.adminDashboardPermission,
        PermissionFixtures.adminTicketPermission,
        PermissionFixtures.adminCommentPermission
      )
    ) { ctx =>
      for {
        _      <- ctx.dashboardMembershipService.addMember(member)
        result <- ctx.dashboardMembershipService.findMember(member.boardId, member.userId)
      } yield result
    }

    assertEquals(
      result,
      Some(
        BoardMemberWithRole(
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
    val member = BoardMemberFixtures.sampleMember

    val result = BoardMembershipServiceFixtures.withBoardMembershipService(
      members = List(member),
      roles = List(RoleFixtures.adminRole),
      permissions = List(PermissionFixtures.adminDashboardPermission)
    ) { ctx =>
      for {
        removed <- ctx.dashboardMembershipService.removeMember(member.boardId, member.userId)
        result  <- ctx.dashboardMembershipService.findMember(member.boardId, member.userId)
      } yield (removed, result)
    }

    assertEquals(result, (true, None))
  }

  test("changeMemberRole updates the role for an existing membership") {
    val member = BoardMemberFixtures.sampleMember

    val result = BoardMembershipServiceFixtures.withBoardMembershipService(
      members = List(member),
      roles = List(RoleFixtures.adminRole, RoleFixtures.viewerRole),
      permissions = List(
        PermissionFixtures.adminDashboardPermission,
        PermissionFixtures.viewerDashboardPermission
      )
    ) { ctx =>
      for {
        updated <- ctx.dashboardMembershipService.changeMemberRole(
                     member.boardId,
                     member.userId,
                     RoleId(3)
                   )
        result  <- ctx.dashboardMembershipService.findMember(member.boardId, member.userId)
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
    val member = BoardMemberFixtures.sampleMember

    val result = BoardMembershipServiceFixtures.withBoardMembershipService(
      members = List(member),
      roles = List(RoleFixtures.adminRole),
      permissions = List(
        PermissionFixtures.adminDashboardPermission,
        PermissionFixtures.adminTicketPermission,
        PermissionFixtures.adminCommentPermission
      )
    ) { ctx =>
      ctx.dashboardMembershipService.findMember(member.boardId, member.userId)
    }

    assertEquals(
      result,
      Some(
        BoardMemberWithRole(
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
    val firstMember  = BoardMemberFixtures.sampleMember
    val secondMember =
      BoardMemberFixtures.member(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = RoleId(3),
        createdAt = Instant.parse("2026-04-06T08:05:00Z")
      )

    val result = BoardMembershipServiceFixtures.withBoardMembershipService(
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
      ctx.dashboardMembershipService.listMembers(firstMember.boardId)
    }

    assertEquals(
      result,
      List(
        BoardMemberWithRole(
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
        BoardMemberWithRole(
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
    val firstMember  = BoardMemberFixtures.sampleMember
    val secondMember =
      BoardMemberFixtures.member(
        boardId = BoardId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")),
        createdAt = Instant.parse("2026-04-06T08:05:00Z")
      )
    val otherMember  =
      BoardMemberFixtures.member(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = RoleId(3)
      )

    val result = BoardMembershipServiceFixtures.withBoardMembershipService(
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
        BoardMemberWithRole(
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
        BoardMemberWithRole(
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
