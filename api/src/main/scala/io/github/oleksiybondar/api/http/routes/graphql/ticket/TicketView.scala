package io.github.oleksiybondar.api.http.routes.graphql.ticket

import io.github.oleksiybondar.api.http.routes.graphql.comment.CommentView
import io.github.oleksiybondar.api.http.routes.graphql.timeTracking.TimeTrackingEntryView
import io.github.oleksiybondar.api.http.routes.graphql.user.UserView

final case class TicketBoardSummaryView(
    id: String,
    name: String,
    active: Boolean
)

final case class TicketView(
    id: String,
    boardId: String,
    name: String,
    description: Option[String],
    acceptanceCriteria: Option[String],
    estimatedMinutes: Option[Int],
    createdByUserId: String,
    assignedToUserId: Option[String],
    lastModifiedByUserId: String,
    createdAt: String,
    modifiedAt: String,
    stateId: Long,
    commentsCount: Int = 0,
    trackedMinutes: Int = 0,
    board: Option[TicketBoardSummaryView] = None,
    createdBy: Option[UserView] = None,
    assignedTo: Option[UserView] = None,
    lastModifiedBy: Option[UserView] = None,
    timeEntries: List[TimeTrackingEntryView] = Nil,
    comments: List[CommentView] = Nil
)
