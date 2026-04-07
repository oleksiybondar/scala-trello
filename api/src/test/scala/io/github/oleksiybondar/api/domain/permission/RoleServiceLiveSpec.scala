package io.github.oleksiybondar.api.domain.permission

import io.github.oleksiybondar.api.testkit.fixtures.{
  PermissionFixtures,
  RoleFixtures,
  RoleServiceFixtures
}
import munit.FunSuite

class RoleServiceLiveSpec extends FunSuite {

  test("getRole returns the matching role") {
    val result = RoleServiceFixtures.withRoleService(List(RoleFixtures.adminRole)) { ctx =>
      ctx.roleService.getRole(RoleId(1))
    }

    assertEquals(result, Some(RoleFixtures.adminRole))
  }

  test("getByName returns the matching role") {
    val result = RoleServiceFixtures.withRoleService(List(RoleFixtures.viewerRole)) { ctx =>
      ctx.roleService.getByName(RoleName("viewer"))
    }

    assertEquals(result, Some(RoleFixtures.viewerRole))
  }

  test("listRoles returns all roles in id order") {
    val result = RoleServiceFixtures.withRoleService(
      List(RoleFixtures.viewerRole, RoleFixtures.adminRole, RoleFixtures.contributorRole)
    ) { ctx =>
      ctx.roleService.listRoles
    }

    assertEquals(
      result,
      List(RoleFixtures.adminRole, RoleFixtures.contributorRole, RoleFixtures.viewerRole)
    )
  }

  test("getRoleWithPermissions assembles the role and its permissions") {
    val result = RoleServiceFixtures.withRoleService(
      roles = List(RoleFixtures.contributorRole),
      permissions = List(
        PermissionFixtures.contributorDashboardPermission,
        PermissionFixtures.contributorTicketPermission,
        PermissionFixtures.contributorCommentPermission
      )
    ) { ctx =>
      ctx.roleService.getRoleWithPermissions(RoleId(2))
    }

    assertEquals(
      result,
      Some(
        RoleWithPermissions(
          RoleFixtures.contributorRole,
          List(
            PermissionFixtures.contributorDashboardPermission,
            PermissionFixtures.contributorTicketPermission,
            PermissionFixtures.contributorCommentPermission
          )
        )
      )
    )
  }

  test("getRoleWithPermissionsByName assembles the matching role by name") {
    val result = RoleServiceFixtures.withRoleService(
      roles = List(RoleFixtures.viewerRole),
      permissions = List(
        PermissionFixtures.viewerDashboardPermission,
        PermissionFixtures.viewerTicketPermission,
        PermissionFixtures.viewerCommentPermission
      )
    ) { ctx =>
      ctx.roleService.getRoleWithPermissionsByName(RoleName("viewer"))
    }

    assertEquals(
      result,
      Some(
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
  }

  test("listRolesWithPermissions assembles every role with its permissions") {
    val result = RoleServiceFixtures.withRoleService(
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
      ctx.roleService.listRolesWithPermissions
    }

    assertEquals(
      result,
      List(
        RoleWithPermissions(
          RoleFixtures.adminRole,
          List(
            PermissionFixtures.adminDashboardPermission,
            PermissionFixtures.adminTicketPermission,
            PermissionFixtures.adminCommentPermission
          )
        ),
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
  }
}
