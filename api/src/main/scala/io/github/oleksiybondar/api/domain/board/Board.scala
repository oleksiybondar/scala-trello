package io.github.oleksiybondar.api.domain.board

import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant
import java.util.UUID

final case class BoardId(value: UUID)            extends AnyVal
final case class BoardName(value: String)        extends AnyVal
final case class BoardDescription(value: String) extends AnyVal

final case class Board(
    id: BoardId,
    name: BoardName,
    description: Option[BoardDescription],
    active: Boolean,
    ownerUserId: UserId,
    createdByUserId: UserId,
    createdAt: Instant,
    modifiedAt: Instant,
    lastModifiedByUserId: UserId
)
