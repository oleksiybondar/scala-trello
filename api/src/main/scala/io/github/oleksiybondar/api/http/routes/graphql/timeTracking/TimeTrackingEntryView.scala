package io.github.oleksiybondar.api.http.routes.graphql.timeTracking

import io.github.oleksiybondar.api.http.routes.graphql.user.UserView

final case class TimeTrackingTicketSummaryView(
    id: String,
    title: String,
    description: Option[String],
    board: Option[TimeTrackingBoardSummaryView] = None
)

final case class TimeTrackingBoardSummaryView(
    id: String,
    title: String,
    active: Boolean
)

final case class TimeTrackingEntryView(
    id: String,
    ticketId: String,
    userId: String,
    activityId: String,
    activityCode: Option[String] = None,
    activityName: Option[String] = None,
    durationMinutes: Int,
    loggedAt: String,
    description: Option[String],
    user: Option[UserView] = None,
    ticket: Option[TimeTrackingTicketSummaryView] = None
)
