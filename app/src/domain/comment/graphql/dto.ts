export interface CommentUserSummaryResponse {
  avatarUrl: string | null;
  firstName: string;
  id: string;
  lastName: string;
}

export interface CommentTicketSummaryResponse {
  boardId: string;
  id: string;
  title: string;
}

export interface CommentResponse {
  authorUserId: string;
  createdAt: string;
  id: string;
  message: string;
  modifiedAt: string;
  relatedCommentId: string | null;
  ticket: CommentTicketSummaryResponse | null;
  ticketId: string;
  user: CommentUserSummaryResponse | null;
}

export interface CommentQueryResponse {
  comment: CommentResponse | null;
}

export interface CommentsQueryResponse {
  comments: CommentResponse[];
}

export interface CommentsByUserQueryResponse {
  commentsByUser: CommentResponse[];
}

export interface PostCommentMutationResponse {
  postComment: CommentResponse;
}

export interface UpdateCommentMessageMutationResponse {
  updateCommentMessage: CommentResponse;
}

export interface DeleteCommentMutationResponse {
  deleteComment: boolean;
}
