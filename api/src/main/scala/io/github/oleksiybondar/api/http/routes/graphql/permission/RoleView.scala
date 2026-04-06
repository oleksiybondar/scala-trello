package io.github.oleksiybondar.api.http.routes.graphql.permission

final case class PermissionView(
    id: String,
    area: String,
    canRead: Boolean,
    canCreate: Boolean,
    canModify: Boolean,
    canDelete: Boolean,
    canReassign: Boolean
)

final case class RoleView(
    id: String,
    name: String,
    description: Option[String],
    permissions: List[PermissionView]
)
