package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.permission.{Role, RoleId, RoleName}

object RoleFixtures {

  val adminRole: Role =
    Role(
      id = RoleId(1),
      name = RoleName("admin"),
      description = Some("Full dashboard access including member management.")
    )

  val contributorRole: Role =
    Role(
      id = RoleId(2),
      name = RoleName("contributor"),
      description = Some("Can contribute to tickets and comments.")
    )

  val viewerRole: Role =
    Role(
      id = RoleId(3),
      name = RoleName("viewer"),
      description = Some("Read-only access to dashboard data.")
    )
}
