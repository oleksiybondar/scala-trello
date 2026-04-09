import type { CreateBoardRequest } from "@models/board/dto";

const DASHBOARD_FIELDS = `
  id
  name
  description
  active
  membersCount
  ownerUserId
  owner {
    id
    firstName
    lastName
    avatarUrl
  }
  createdByUserId
  createdBy {
    id
    firstName
    lastName
    avatarUrl
  }
  createdAt
  modifiedAt
  lastModifiedByUserId
`;

const escapeGraphQLString = (value: string): string => {
  return value.replace(/\\/g, "\\\\").replace(/"/g, '\\"');
};

const serializeGraphQLString = (value: string): string => {
  return `"${escapeGraphQLString(value)}"`;
};

const serializeNullableGraphQLString = (value: string | undefined): string => {
  return value === undefined ? "null" : serializeGraphQLString(value);
};

export const buildMyDashboardsQuery = (): string => {
  return /* GraphQL */ `
    query {
      myDashboards {
        ${DASHBOARD_FIELDS}
      }
    }
  `;
};

export const buildCreateDashboardMutation = (
  request: CreateBoardRequest
): string => {
  return /* GraphQL */ `
    mutation {
      createDashboard(
        name: ${serializeGraphQLString(request.name)}
        description: ${serializeNullableGraphQLString(request.description)}
      ) {
        ${DASHBOARD_FIELDS}
      }
    }
  `;
};
