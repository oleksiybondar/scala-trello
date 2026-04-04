package io.github.oleksiybondar.api.infrastructure.db.permission

import io.github.oleksiybondar.api.domain.permission.{PermissionArea, PermissionId, RoleId}
import io.github.oleksiybondar.api.testkit.fixtures.PermissionFixtures
import io.github.oleksiybondar.api.testkit.fixtures.SlickPermissionRepoFixtures.withRepo
import munit.FunSuite

class SlickPermissionRepoSpec extends FunSuite {

  test("findById returns the seeded permission") {
    withRepo { repo =>
      repo.findById(PermissionId(1)).map { result =>
        assertEquals(result, Some(PermissionFixtures.adminDashboardPermission))
      }
    }
  }

  test("findByRoleId returns all seeded permissions for the role") {
    withRepo { repo =>
      repo.findByRoleId(RoleId(2)).map { result =>
        assertEquals(
          result,
          List(
            PermissionFixtures.contributorDashboardPermission,
            PermissionFixtures.contributorTicketPermission,
            PermissionFixtures.contributorCommentPermission
          )
        )
      }
    }
  }

  test("findByRoleIdAndArea returns the matching seeded permission") {
    withRepo { repo =>
      repo
        .findByRoleIdAndArea(RoleId(3), PermissionArea.Comment)
        .map { result =>
          assertEquals(result, Some(PermissionFixtures.viewerCommentPermission))
        }
    }
  }

  test("list returns all seeded permissions in id order") {
    withRepo { repo =>
      repo.list.map { result =>
        assertEquals(
          result,
          List(
            PermissionFixtures.adminDashboardPermission,
            PermissionFixtures.adminTicketPermission,
            PermissionFixtures.adminCommentPermission,
            PermissionFixtures.contributorDashboardPermission,
            PermissionFixtures.contributorTicketPermission,
            PermissionFixtures.contributorCommentPermission,
            PermissionFixtures.viewerDashboardPermission,
            PermissionFixtures.viewerTicketPermission,
            PermissionFixtures.viewerCommentPermission
          )
        )
      }
    }
  }
}
