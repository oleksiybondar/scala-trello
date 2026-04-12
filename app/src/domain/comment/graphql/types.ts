export interface CommentUserSummary {
  avatarUrl: string | null;
  firstName: string;
  lastName: string;
  userId: string;
}

export interface CommentTicketSummary {
  boardId: string;
  ticketId: string;
  title: string;
}

export interface Comment {
  authorUserId: string;
  commentId: string;
  createdAt: string;
  message: string;
  modifiedAt: string;
  relatedCommentId: string | null;
  ticket: CommentTicketSummary | null;
  ticketId: string;
  user: CommentUserSummary | null;
}
