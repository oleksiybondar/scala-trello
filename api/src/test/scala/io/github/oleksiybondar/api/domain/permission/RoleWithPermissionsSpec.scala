package io.github.oleksiybondar.api.domain.permission

import io.github.oleksiybondar.api.testkit.fixtures.{PermissionFixtures, RoleFixtures}
import munit.FunSuite

class RoleWithPermissionsSpec extends FunSuite {

  test("canRead returns true when the matching area allows read") {
    val roleWithPermissions =
      RoleWithPermissions(
        role = RoleFixtures.viewerRole,
        permissions = List(PermissionFixtures.viewerDashboardPermission)
      )

    assertEquals(roleWithPermissions.canRead(PermissionArea.Board), true)
  }

  test("canCreate returns false when the matching area denies create") {
    val roleWithPermissions =
      RoleWithPermissions(
        role = RoleFixtures.viewerRole,
        permissions = List(PermissionFixtures.viewerTicketPermission)
      )

    assertEquals(roleWithPermissions.canCreate(PermissionArea.Ticket), false)
  }

  test("canModify returns true when the matching area allows modify") {
    val roleWithPermissions =
      RoleWithPermissions(
        role = RoleFixtures.contributorRole,
        permissions = List(PermissionFixtures.contributorCommentPermission)
      )

    assertEquals(roleWithPermissions.canModify(PermissionArea.Comment), true)
  }

  test("canDelete returns false when there is no permission entry for the area") {
    val roleWithPermissions =
      RoleWithPermissions(
        role = RoleFixtures.adminRole,
        permissions = List(PermissionFixtures.adminDashboardPermission)
      )

    assertEquals(roleWithPermissions.canDelete(PermissionArea.Comment), false)
  }

  test("canReassign returns the matching permission flag") {
    val roleWithPermissions =
      RoleWithPermissions(
        role = RoleFixtures.contributorRole,
        permissions = List(PermissionFixtures.contributorTicketPermission)
      )

    assertEquals(roleWithPermissions.canReassign(PermissionArea.Ticket), true)
  }
}
