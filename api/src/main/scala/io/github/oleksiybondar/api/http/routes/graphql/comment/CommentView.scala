package io.github.oleksiybondar.api.http.routes.graphql.comment

final case class CommentTicketSummaryView(
    id: String,
    boardId: String,
    title: String
)

final case class CommentView(
    id: String,
    ticketId: String,
    authorUserId: String,
    createdAt: String,
    modifiedAt: String,
    message: String,
    relatedCommentId: Option[String]
)
