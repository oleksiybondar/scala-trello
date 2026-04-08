package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.board.{BoardId, BoardMember}
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant
import java.util.UUID

object BoardMemberFixtures {

  val sampleMember: BoardMember =
    BoardMember(
      dashboardId = BoardId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
      userId = UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
      roleId = RoleId(1),
      createdAt = Instant.parse("2026-04-06T08:00:00Z")
    )

  def member(
      dashboardId: BoardId = sampleMember.dashboardId,
      userId: UserId = sampleMember.userId,
      roleId: RoleId = sampleMember.roleId,
      createdAt: Instant = sampleMember.createdAt
  ): BoardMember =
    BoardMember(
      dashboardId = dashboardId,
      userId = userId,
      roleId = roleId,
      createdAt = createdAt
    )
}
