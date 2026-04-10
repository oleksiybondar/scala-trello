package io.github.oleksiybondar.api.http.routes.graphql.board

import io.github.oleksiybondar.api.domain.permission.Permission
import io.github.oleksiybondar.api.http.routes.graphql.ticket.TicketView
import io.github.oleksiybondar.api.http.routes.graphql.user.UserView

final case class BoardView(
    id: String,
    name: String,
    description: Option[String],
    active: Boolean,
    ownerUserId: String,
    createdByUserId: String,
    createdAt: String,
    modifiedAt: String,
    lastModifiedByUserId: String,
    owner: Option[UserView] = None,
    createdBy: Option[UserView] = None,
    lastModifiedBy: Option[UserView] = None,
    membersCount: Int = 0,
    currentUserRole: Option[BoardRoleView] = None,
    tickets: List[TicketView] = Nil
)

final case class BoardRoleView(
    id: String,
    name: String,
    description: Option[String],
    permissions: List[Permission] = Nil
)

final case class BoardMemberView(
    boardId: String,
    userId: String,
    createdAt: String,
    role: BoardRoleView,
    user: Option[UserView]
)
