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

const TIME_TRACKING_ENTRY_FIELDS = `
  id
  ticketId
  userId
  activityId
  activityCode
  activityName
  durationMinutes
  loggedAt
  description
  user {
    ${USER_FIELDS}
  }
  ticket {
    id
    title
    description
  }
`;

const TICKET_FIELDS = `
  id
  boardId
  name
  description
  acceptanceCriteria
  estimatedMinutes
  commentsCount
  trackedMinutes
  status
  createdByUserId
  assignedToUserId
  lastModifiedByUserId
  createdAt
  modifiedAt
  board {
    id
    name
    active
  }
  createdBy {
    ${USER_FIELDS}
  }
  assignedTo {
    ${USER_FIELDS}
  }
  lastModifiedBy {
    ${USER_FIELDS}
  }
  comments {
    ${COMMENT_FIELDS}
  }
  timeEntries {
    ${TIME_TRACKING_ENTRY_FIELDS}
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

const serializeNullableGraphQLInt = (value: number | null): string => {
  return value === null ? "null" : String(value);
};

export const buildTicketQuery = (ticketId: string): string => {
  return /* GraphQL */ `
    query {
      ticket(ticketId: ${serializeGraphQLString(ticketId)}) {
        ${TICKET_FIELDS}
      }
    }
  `;
};

export const buildTicketsQuery = (boardId: string): string => {
  return /* GraphQL */ `
    query {
      tickets(boardId: ${serializeGraphQLString(boardId)}) {
        ${TICKET_FIELDS}
      }
    }
  `;
};

interface BuildCreateTicketMutationParams {
  acceptanceCriteria?: string | null;
  assignedToUserId?: string | null;
  boardId: string;
  description?: string | null;
  estimatedMinutes?: number | null;
  title: string;
}

export const buildCreateTicketMutation = ({
  acceptanceCriteria = null,
  assignedToUserId = null,
  boardId,
  description = null,
  estimatedMinutes = null,
  title
}: BuildCreateTicketMutationParams): string => {
  return /* GraphQL */ `
    mutation {
      createTicket(
        boardId: ${serializeGraphQLString(boardId)}
        title: ${serializeGraphQLString(title)}
        description: ${serializeNullableGraphQLString(description)}
        acceptanceCriteria: ${serializeNullableGraphQLString(acceptanceCriteria)}
        estimatedMinutes: ${serializeNullableGraphQLInt(estimatedMinutes)}
        assignedToUserId: ${serializeNullableGraphQLString(assignedToUserId)}
      ) {
        ${TICKET_FIELDS}
      }
    }
  `;
};

export const buildChangeTicketTitleMutation = (
  ticketId: string,
  title: string
): string => {
  return /* GraphQL */ `
    mutation {
      changeTicketTitle(
        ticketId: ${serializeGraphQLString(ticketId)}
        title: ${serializeGraphQLString(title)}
      ) {
        ${TICKET_FIELDS}
      }
    }
  `;
};

export const buildChangeTicketDescriptionMutation = (
  ticketId: string,
  description: string | null
): string => {
  return /* GraphQL */ `
    mutation {
      changeTicketDescription(
        ticketId: ${serializeGraphQLString(ticketId)}
        description: ${serializeNullableGraphQLString(description)}
      ) {
        ${TICKET_FIELDS}
      }
    }
  `;
};

export const buildChangeTicketAcceptanceCriteriaMutation = (
  ticketId: string,
  acceptanceCriteria: string | null
): string => {
  return /* GraphQL */ `
    mutation {
      changeTicketAcceptanceCriteria(
        ticketId: ${serializeGraphQLString(ticketId)}
        acceptanceCriteria: ${serializeNullableGraphQLString(acceptanceCriteria)}
      ) {
        ${TICKET_FIELDS}
      }
    }
  `;
};

export const buildChangeTicketEstimatedTimeMutation = (
  ticketId: string,
  estimatedMinutes: number | null
): string => {
  return /* GraphQL */ `
    mutation {
      changeTicketEstimatedTime(
        ticketId: ${serializeGraphQLString(ticketId)}
        estimatedMinutes: ${serializeNullableGraphQLInt(estimatedMinutes)}
      ) {
        ${TICKET_FIELDS}
      }
    }
  `;
};

export const buildChangeTicketStatusMutation = (
  ticketId: string,
  status: string
): string => {
  return /* GraphQL */ `
    mutation {
      changeTicketStatus(
        ticketId: ${serializeGraphQLString(ticketId)}
        status: ${serializeGraphQLString(status)}
      ) {
        ${TICKET_FIELDS}
      }
    }
  `;
};

export const buildReassignTicketMutation = (
  ticketId: string,
  assignedToUserId: string | null
): string => {
  return /* GraphQL */ `
    mutation {
      reassignTicket(
        ticketId: ${serializeGraphQLString(ticketId)}
        assignedToUserId: ${serializeNullableGraphQLString(assignedToUserId)}
      ) {
        ${TICKET_FIELDS}
      }
    }
  `;
};
