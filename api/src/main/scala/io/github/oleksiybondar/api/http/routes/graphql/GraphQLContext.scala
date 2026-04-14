package io.github.oleksiybondar.api.http.routes.graphql

import cats.effect.IO
import io.github.oleksiybondar.api.domain.auth.AuthService
import io.github.oleksiybondar.api.domain.board.{
  BoardAccessService,
  BoardMembershipService,
  BoardService
}
import io.github.oleksiybondar.api.domain.comment.CommentService
import io.github.oleksiybondar.api.domain.permission.{PermissionService, RoleService}
import io.github.oleksiybondar.api.domain.ticket.TicketService
import io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingService
import io.github.oleksiybondar.api.domain.user.{UserId, UserService}
import io.github.oleksiybondar.api.infrastructure.db.board.BoardQueryRepo
import io.github.oleksiybondar.api.infrastructure.db.comment.CommentQueryRepo
import io.github.oleksiybondar.api.infrastructure.db.permission.RoleQueryRepo
import io.github.oleksiybondar.api.infrastructure.db.ticket.{
  TicketQueryRepo,
  TicketSeverityRepo,
  TicketStateRepo
}
import io.github.oleksiybondar.api.infrastructure.db.timeTracking.{
  TimeTrackingActivityRepo,
  TimeTrackingQueryRepo
}

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
    /** Joined board read repo used for detailed board projections. */
    boardQueryRepo: BoardQueryRepo[IO],
    /** Role and permission service exposed to GraphQL dictionary queries. */
    roleService: RoleService[IO],
    /** Joined role read repo used for optimized role/permission reads. */
    roleQueryRepo: RoleQueryRepo[IO],
    /** Permission service exposed to GraphQL dictionary queries. */
    permissionService: PermissionService[IO],
    /** Ticket service exposed to ticket queries and mutations. */
    ticketService: TicketService[IO],
    /** Joined ticket read repo used for detailed ticket projections. */
    ticketQueryRepo: TicketQueryRepo[IO],
    /** Ticket state repo used for status/name expansion in GraphQL views. */
    ticketStateRepo: TicketStateRepo[IO],
    /** Ticket severity repo exposed to dictionary queries. */
    ticketSeverityRepo: TicketSeverityRepo[IO],
    /** Time tracking activity repo exposed to dictionary queries. */
    timeTrackingActivityRepo: TimeTrackingActivityRepo[IO],
    /** Time tracking service exposed to time entry queries and mutations. */
    timeTrackingService: TimeTrackingService[IO],
    /** Joined comment read repo used for optimized nested reads. */
    commentQueryRepo: CommentQueryRepo[IO],
    /** Joined time tracking read repo used for optimized nested reads. */
    timeTrackingQueryRepo: TimeTrackingQueryRepo[IO],
    /** Comment service exposed to comment queries and mutations. */
    commentService: CommentService[IO],
    /** Authentication service available for GraphQL flows that need token-related behavior. */
    authService: AuthService[IO],
    /** User id extracted from the already verified bearer token, when present. */
    currentUserId: Option[UserId]
)
