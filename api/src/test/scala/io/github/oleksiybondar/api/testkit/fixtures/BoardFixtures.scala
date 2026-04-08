package io.github.oleksiybondar.api.testkit.fixtures

import io.github.oleksiybondar.api.domain.board.{Board, BoardDescription, BoardId, BoardName}
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant
import java.util.UUID

object BoardFixtures {

  val sampleDashboard: Board =
    Board(
      id = BoardId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
      name = BoardName("Core Board"),
      description = Some(BoardDescription("Main project dashboard")),
      active = true,
      ownerUserId = UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
      createdByUserId = UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
      createdAt = Instant.parse("2026-04-05T08:00:00Z"),
      modifiedAt = Instant.parse("2026-04-05T08:00:00Z"),
      lastModifiedByUserId = UserId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
    )

  def dashboard(
      id: BoardId = sampleDashboard.id,
      name: BoardName = sampleDashboard.name,
      description: Option[BoardDescription] = sampleDashboard.description,
      active: Boolean = sampleDashboard.active,
      ownerUserId: UserId = sampleDashboard.ownerUserId,
      createdByUserId: UserId = sampleDashboard.createdByUserId,
      createdAt: Instant = sampleDashboard.createdAt,
      modifiedAt: Instant = sampleDashboard.modifiedAt,
      lastModifiedByUserId: UserId = sampleDashboard.lastModifiedByUserId
  ): Board =
    Board(
      id = id,
      name = name,
      description = description,
      active = active,
      ownerUserId = ownerUserId,
      createdByUserId = createdByUserId,
      createdAt = createdAt,
      modifiedAt = modifiedAt,
      lastModifiedByUserId = lastModifiedByUserId
    )
}
