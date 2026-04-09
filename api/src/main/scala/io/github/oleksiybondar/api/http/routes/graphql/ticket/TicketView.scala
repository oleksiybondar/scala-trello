package io.github.oleksiybondar.api.http.routes.graphql.ticket

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
    stateId: Long
)
