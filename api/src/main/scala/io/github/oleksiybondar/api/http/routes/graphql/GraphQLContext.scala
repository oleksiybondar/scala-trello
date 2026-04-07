package io.github.oleksiybondar.api.http.routes.graphql

import cats.effect.IO
import io.github.oleksiybondar.api.domain.auth.AuthService
import io.github.oleksiybondar.api.domain.permission.{PermissionService, RoleService}
import io.github.oleksiybondar.api.domain.user.{UserId, UserService}

/** Per-request GraphQL context carrying services and the authenticated user identity. */
final case class GraphQLContext(
    /** User-facing application service used by queries and mutations. */
    userService: UserService[IO],
    /** Role and permission service exposed to GraphQL dictionary queries. */
    roleService: RoleService[IO],
    /** Permission service exposed to GraphQL dictionary queries. */
    permissionService: PermissionService[IO],
    /** Authentication service available for GraphQL flows that need token-related behavior. */
    authService: AuthService[IO],
    /** User id extracted from the already verified bearer token, when present. */
    currentUserId: Option[UserId]
)
