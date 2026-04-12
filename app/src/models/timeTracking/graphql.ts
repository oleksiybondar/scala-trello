const USER_FIELDS = `
  id
  firstName
  lastName
  avatarUrl
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
  ticket {
    id
    title
    description
  }
  user {
    ${USER_FIELDS}
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

export const buildTimeTrackingEntryQuery = (entryId: string): string => {
  return /* GraphQL */ `
    query {
      timeTrackingEntry(entryId: ${serializeGraphQLString(entryId)}) {
        ${TIME_TRACKING_ENTRY_FIELDS}
      }
    }
  `;
};

export const buildTimeTrackingEntriesByUserQuery = (userId: string): string => {
  return /* GraphQL */ `
    query {
      timeTrackingEntriesByUser(userId: ${serializeGraphQLString(userId)}) {
        ${TIME_TRACKING_ENTRY_FIELDS}
      }
    }
  `;
};

export const buildTimeTrackingEntriesByTicketQuery = (ticketId: string): string => {
  return /* GraphQL */ `
    query {
      timeTrackingEntriesByTicket(ticketId: ${serializeGraphQLString(ticketId)}) {
        ${TIME_TRACKING_ENTRY_FIELDS}
      }
    }
  `;
};

interface BuildCreateTimeTrackingEntryMutationParams {
  activityId: number;
  description?: string | null;
  durationMinutes: number;
  loggedAt: string;
  ticketId: string;
}

export const buildCreateTimeTrackingEntryMutation = ({
  activityId,
  description = null,
  durationMinutes,
  loggedAt,
  ticketId
}: BuildCreateTimeTrackingEntryMutationParams): string => {
  return /* GraphQL */ `
    mutation {
      createTimeTrackingEntry(
        ticketId: ${serializeGraphQLString(ticketId)}
        activityId: ${String(activityId)}
        durationMinutes: ${String(durationMinutes)}
        loggedAt: ${serializeGraphQLString(loggedAt)}
        description: ${serializeNullableGraphQLString(description)}
      ) {
        ${TIME_TRACKING_ENTRY_FIELDS}
      }
    }
  `;
};

export const buildUpdateTimeTrackingActivityMutation = (
  entryId: string,
  activityId: number
): string => {
  return /* GraphQL */ `
    mutation {
      updateTimeTrackingActivity(
        entryId: ${serializeGraphQLString(entryId)}
        activityId: ${String(activityId)}
      ) {
        ${TIME_TRACKING_ENTRY_FIELDS}
      }
    }
  `;
};

export const buildUpdateTimeTrackingDescriptionMutation = (
  entryId: string,
  description: string | null
): string => {
  return /* GraphQL */ `
    mutation {
      updateTimeTrackingDescription(
        entryId: ${serializeGraphQLString(entryId)}
        description: ${serializeNullableGraphQLString(description)}
      ) {
        ${TIME_TRACKING_ENTRY_FIELDS}
      }
    }
  `;
};

export const buildUpdateTimeTrackingTimeMutation = (
  entryId: string,
  durationMinutes: number,
  loggedAt: string
): string => {
  return /* GraphQL */ `
    mutation {
      updateTimeTrackingTime(
        entryId: ${serializeGraphQLString(entryId)}
        durationMinutes: ${String(durationMinutes)}
        loggedAt: ${serializeGraphQLString(loggedAt)}
      ) {
        ${TIME_TRACKING_ENTRY_FIELDS}
      }
    }
  `;
};

export const buildDeleteTimeTrackingEntryMutation = (entryId: string): string => {
  return /* GraphQL */ `
    mutation {
      deleteTimeTrackingEntry(entryId: ${serializeGraphQLString(entryId)})
    }
  `;
};
