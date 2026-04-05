package io.github.oleksiybondar.api.infrastructure.db.permission

import io.github.oleksiybondar.api.domain.permission.{RoleId, RoleName}
import io.github.oleksiybondar.api.testkit.fixtures.RoleFixtures
import io.github.oleksiybondar.api.testkit.fixtures.SlickRoleRepoFixtures.withRepo
import munit.FunSuite

class SlickRoleRepoSpec extends FunSuite {

  test("findById returns the seeded role") {
    withRepo { repo =>
      repo.findById(RoleId(1)).map { result =>
        assertEquals(result, Some(RoleFixtures.adminRole))
      }
    }
  }

  test("findByName returns the matching seeded role") {
    withRepo { repo =>
      repo.findByName(RoleName("viewer")).map { result =>
        assertEquals(result, Some(RoleFixtures.viewerRole))
      }
    }
  }

  test("list returns all seeded roles in id order") {
    withRepo { repo =>
      repo.list.map { result =>
        assertEquals(
          result,
          List(
            RoleFixtures.adminRole,
            RoleFixtures.contributorRole,
            RoleFixtures.viewerRole
          )
        )
      }
    }
  }
}
