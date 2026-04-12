const USER_FIELDS = `
  id
  firstName
  lastName
  avatarUrl
`;

const COMMENT_FIELDS = `
  id
  ticketId
  authorUserId
  createdAt
  modifiedAt
  message
  relatedCommentId
  user {
    ${USER_FIELDS}
  }
  ticket {
    id
    boardId
    title
  }
`;

const escapeGraphQLString = (value: string): string => {
  return value.replace(/\\/g, "\\\\").replace(/"/g, '\\"');
};

const serializeGraphQLString = (value: string): string => {
  return `"${escapeGraphQLString(value)}"`;
};

const serializeNullableGraphQLString = (value: string | null): string => {
  return value === null ? "null" : serializeGraphQLString(value);
};

export const buildCommentQuery = (commentId: string): string => {
  return /* GraphQL */ `
    query {
      comment(commentId: ${serializeGraphQLString(commentId)}) {
        ${COMMENT_FIELDS}
      }
    }
  `;
};

export const buildCommentsQuery = (ticketId: string): string => {
  return /* GraphQL */ `
    query {
      comments(ticketId: ${serializeGraphQLString(ticketId)}) {
        ${COMMENT_FIELDS}
      }
    }
  `;
};

export const buildCommentsByUserQuery = (userId: string): string => {
  return /* GraphQL */ `
    query {
      commentsByUser(userId: ${serializeGraphQLString(userId)}) {
        ${COMMENT_FIELDS}
      }
    }
  `;
};

export const buildPostCommentMutation = (
  ticketId: string,
  message: string,
  relatedCommentId: string | null
): string => {
  return /* GraphQL */ `
    mutation {
      postComment(
        ticketId: ${serializeGraphQLString(ticketId)}
        message: ${serializeGraphQLString(message)}
        relatedCommentId: ${serializeNullableGraphQLString(relatedCommentId)}
      ) {
        ${COMMENT_FIELDS}
      }
    }
  `;
};

export const buildUpdateCommentMessageMutation = (
  commentId: string,
  message: string
): string => {
  return /* GraphQL */ `
    mutation {
      updateCommentMessage(
        commentId: ${serializeGraphQLString(commentId)}
        message: ${serializeGraphQLString(message)}
      ) {
        ${COMMENT_FIELDS}
      }
    }
  `;
};

export const buildDeleteCommentMutation = (commentId: string): string => {
  return /* GraphQL */ `
    mutation {
      deleteComment(commentId: ${serializeGraphQLString(commentId)})
    }
  `;
};
