import type {
  CommentResponse,
  CommentUserSummaryResponse
} from "./dto";
import type { Comment, CommentTicketSummary, CommentUserSummary } from "./types";

const mapCommentUserSummary = (
  response: CommentUserSummaryResponse | null
): CommentUserSummary | null => {
  if (response === null) {
    return null;
  }

  return {
    avatarUrl: response.avatarUrl,
    firstName: response.firstName,
    lastName: response.lastName,
    userId: response.id
  };
};

const mapCommentTicketSummary = (
  response: CommentResponse["ticket"]
): CommentTicketSummary | null => {
  if (response === null) {
    return null;
  }

  return {
    boardId: response.boardId,
    ticketId: response.id,
    title: response.title
  };
};

export const mapCommentResponseToComment = (response: CommentResponse): Comment => {
  return {
    authorUserId: response.authorUserId,
    commentId: response.id,
    createdAt: response.createdAt,
    message: response.message,
    modifiedAt: response.modifiedAt,
    relatedCommentId: response.relatedCommentId,
    ticket: mapCommentTicketSummary(response.ticket),
    ticketId: response.ticketId,
    user: mapCommentUserSummary(response.user)
  };
};
