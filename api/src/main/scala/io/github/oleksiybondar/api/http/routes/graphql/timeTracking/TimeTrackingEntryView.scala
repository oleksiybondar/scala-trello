package io.github.oleksiybondar.api.http.routes.graphql.timeTracking

import io.github.oleksiybondar.api.http.routes.graphql.user.UserView

final case class TimeTrackingTicketSummaryView(
    id: String,
    title: String,
    description: Option[String]
)

final case class TimeTrackingEntryView(
    id: String,
    ticketId: String,
    userId: String,
    activityId: String,
    durationMinutes: Int,
    loggedAt: String,
    description: Option[String],
    user: Option[UserView] = None,
    ticket: Option[TimeTrackingTicketSummaryView] = None
)
