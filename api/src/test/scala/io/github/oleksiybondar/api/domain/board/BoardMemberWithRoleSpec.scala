package io.github.oleksiybondar.api.domain.board

import io.github.oleksiybondar.api.domain.permission.{PermissionArea, RoleWithPermissions}
import io.github.oleksiybondar.api.testkit.fixtures.{
  BoardMemberFixtures,
  PermissionFixtures,
  RoleFixtures
}
import munit.FunSuite

class BoardMemberWithRoleSpec extends FunSuite {

  test("canRead delegates to the underlying role permissions") {
    val memberWithRole =
      BoardMemberWithRole(
        member = BoardMemberFixtures.sampleMember,
        role = RoleWithPermissions(
          role = RoleFixtures.viewerRole,
          permissions = List(PermissionFixtures.viewerDashboardPermission)
        )
      )

    assertEquals(memberWithRole.canRead(PermissionArea.Board), true)
  }

  test("canCreate delegates to the underlying role permissions") {
    val memberWithRole =
      BoardMemberWithRole(
        member = BoardMemberFixtures.sampleMember,
        role = RoleWithPermissions(
          role = RoleFixtures.contributorRole,
          permissions = List(PermissionFixtures.contributorTicketPermission)
        )
      )

    assertEquals(memberWithRole.canCreate(PermissionArea.Ticket), true)
  }

  test("canModify delegates to the underlying role permissions") {
    val memberWithRole =
      BoardMemberWithRole(
        member = BoardMemberFixtures.sampleMember,
        role = RoleWithPermissions(
          role = RoleFixtures.contributorRole,
          permissions = List(PermissionFixtures.contributorCommentPermission)
        )
      )

    assertEquals(memberWithRole.canModify(PermissionArea.Comment), true)
  }

  test("canDelete returns false when the role denies the permission") {
    val memberWithRole =
      BoardMemberWithRole(
        member = BoardMemberFixtures.sampleMember,
        role = RoleWithPermissions(
          role = RoleFixtures.viewerRole,
          permissions = List(PermissionFixtures.viewerTicketPermission)
        )
      )

    assertEquals(memberWithRole.canDelete(PermissionArea.Ticket), false)
  }

  test("canReassign returns false when the area is missing from the role") {
    val memberWithRole =
      BoardMemberWithRole(
        member = BoardMemberFixtures.sampleMember,
        role = RoleWithPermissions(
          role = RoleFixtures.adminRole,
          permissions = List(PermissionFixtures.adminDashboardPermission)
        )
      )

    assertEquals(memberWithRole.canReassign(PermissionArea.Comment), false)
  }
}
