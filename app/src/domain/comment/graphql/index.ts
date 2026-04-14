export {
  buildCommentQuery,
  buildCommentsByUserQuery,
  buildCommentsQuery,
  buildDeleteCommentMutation,
  buildPostCommentMutation,
  buildUpdateCommentMessageMutation
} from "./graphql";
export { mapCommentResponseToComment } from "./mappers";
export type {
  CommentQueryResponse,
  CommentResponse,
  CommentsByUserQueryResponse,
  CommentsQueryResponse,
  CommentTicketSummaryResponse,
  CommentUserSummaryResponse,
  DeleteCommentMutationResponse,
  PostCommentMutationResponse,
  UpdateCommentMessageMutationResponse
} from "./dto";
export type {
  Comment,
  CommentTicketSummary,
  CommentUserSummary
} from "./types";
