package io.github.oleksiybondar.api.http.routes.graphql.dashboard

final case class DashboardView(
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

final case class DashboardRoleView(
    id: String,
    name: String,
    description: Option[String]
)

final case class DashboardMemberView(
    dashboardId: String,
    userId: String,
    createdAt: String,
    role: DashboardRoleView
)
