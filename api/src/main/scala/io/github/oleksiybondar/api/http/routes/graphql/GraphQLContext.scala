package io.github.oleksiybondar.api.http.routes.graphql

import cats.effect.IO
import io.github.oleksiybondar.api.domain.auth.AuthService
import io.github.oleksiybondar.api.domain.board.{
  BoardAccessService,
  BoardMembershipService,
  BoardService
}
import io.github.oleksiybondar.api.domain.permission.{PermissionService, RoleService}
import io.github.oleksiybondar.api.domain.ticket.TicketService
import io.github.oleksiybondar.api.domain.user.{UserId, UserService}
import io.github.oleksiybondar.api.infrastructure.db.ticket.TicketStateRepo

/** Per-request GraphQL context carrying services and the authenticated user identity. */
final case class GraphQLContext(
    /** User-facing application service used by queries and mutations. */
    userService: UserService[IO],
    /** Board lifecycle service available to GraphQL dashboard queries and mutations. */
    dashboardService: BoardService[IO],
    /** Board membership service available to GraphQL dashboard queries and mutations. */
    dashboardMembershipService: BoardMembershipService[IO],
    /** Board access service available to GraphQL dashboard queries and mutations. */
    dashboardAccessService: BoardAccessService[IO],
    /** Role and permission service exposed to GraphQL dictionary queries. */
    roleService: RoleService[IO],
    /** Permission service exposed to GraphQL dictionary queries. */
    permissionService: PermissionService[IO],
    /** Ticket service exposed to ticket queries and mutations. */
    ticketService: TicketService[IO],
    /** Ticket state repo used for status/name expansion in GraphQL views. */
    ticketStateRepo: TicketStateRepo[IO],
    /** Authentication service available for GraphQL flows that need token-related behavior. */
    authService: AuthService[IO],
    /** User id extracted from the already verified bearer token, when present. */
    currentUserId: Option[UserId]
)
