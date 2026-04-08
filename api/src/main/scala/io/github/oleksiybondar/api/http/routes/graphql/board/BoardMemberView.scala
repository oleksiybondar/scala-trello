package io.github.oleksiybondar.api.http.routes.graphql.board

final case class BoardView(
    id: String,
    name: String,
    description: Option[String],
    active: Boolean,
    ownerUserId: String,
    createdByUserId: String,
    createdAt: String,
    modifiedAt: String,
    lastModifiedByUserId: String
)

final case class BoardRoleView(
    id: String,
    name: String,
    description: Option[String]
)

final case class BoardMemberView(
    boardId: String,
    userId: String,
    createdAt: String,
    role: BoardRoleView
)
