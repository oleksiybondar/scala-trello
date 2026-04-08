package io.github.oleksiybondar.api.domain.board

import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId

import java.time.Instant

final case class BoardMember(
    boardId: BoardId,
    userId: UserId,
    roleId: RoleId,
    createdAt: Instant
)
