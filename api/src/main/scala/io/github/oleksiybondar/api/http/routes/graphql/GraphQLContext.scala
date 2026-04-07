package io.github.oleksiybondar.api.http.routes.graphql

import cats.effect.IO
import io.github.oleksiybondar.api.domain.auth.AuthService
import io.github.oleksiybondar.api.domain.dashboard.{
  DashboardAccessService,
  DashboardMembershipService,
  DashboardService
}
import io.github.oleksiybondar.api.domain.permission.{PermissionService, RoleService}
import io.github.oleksiybondar.api.domain.user.{UserId, UserService}

/** Per-request GraphQL context carrying services and the authenticated user identity. */
final case class GraphQLContext(
    /** User-facing application service used by queries and mutations. */
    userService: UserService[IO],
    /** Dashboard lifecycle service available to GraphQL dashboard queries and mutations. */
    dashboardService: DashboardService[IO],
    /** Dashboard membership service available to GraphQL dashboard queries and mutations. */
    dashboardMembershipService: DashboardMembershipService[IO],
    /** Dashboard access service available to GraphQL dashboard queries and mutations. */
    dashboardAccessService: DashboardAccessService[IO],
    /** Role and permission service exposed to GraphQL dictionary queries. */
    roleService: RoleService[IO],
    /** Permission service exposed to GraphQL dictionary queries. */
    permissionService: PermissionService[IO],
    /** Authentication service available for GraphQL flows that need token-related behavior. */
    authService: AuthService[IO],
    /** User id extracted from the already verified bearer token, when present. */
    currentUserId: Option[UserId]
)
