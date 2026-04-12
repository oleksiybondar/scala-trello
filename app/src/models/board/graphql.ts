import type { CreateBoardRequest } from "@models/board/dto";

const BOARD_FIELDS = `
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
  currentUserRole {
    id
    name
    description
    permissions {
      id
      area
      canRead
      canCreate
      canModify
      canDelete
      canReassign
    }
  }
  tickets {
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
    createdBy {
      id
      firstName
      lastName
      avatarUrl
    }
    assignedTo {
      id
      firstName
      lastName
      avatarUrl
    }
    lastModifiedBy {
      id
      firstName
      lastName
      avatarUrl
    }
    timeEntries {
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
        id
        firstName
        lastName
        avatarUrl
      }
      ticket {
        id
        title
        description
      }
    }
  }
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

interface BuildMyBoardsQueryParams {
  active?: boolean | undefined;
  keyword?: string | undefined;
  ownerUserId?: string | undefined;
}

const buildMyBoardsArguments = ({
  active,
  keyword,
  ownerUserId
}: BuildMyBoardsQueryParams): string => {
  const args = [
    `active: ${active === undefined ? "null" : String(active)}`,
    `keyword: ${serializeNullableGraphQLString(keyword)}`,
    `ownerUserId: ${serializeNullableGraphQLString(ownerUserId)}`
  ];

  return args.join("\n        ");
};

export const buildMyBoardsQuery = (
  params: BuildMyBoardsQueryParams = {}
): string => {
  return /* GraphQL */ `
    query {
      myBoards(
        ${buildMyBoardsArguments(params)}
      ) {
        ${BOARD_FIELDS}
      }
    }
  `;
};

export const buildBoardQuery = (boardId: string): string => {
  return /* GraphQL */ `
    query {
      board(boardId: ${serializeGraphQLString(boardId)}) {
        ${BOARD_FIELDS}
      }
    }
  `;
};

export const buildBoardMembersQuery = (boardId: string): string => {
  return /* GraphQL */ `
    query {
      boardMembers(boardId: ${serializeGraphQLString(boardId)}) {
        boardId
        userId
        createdAt
        user {
          id
          firstName
          lastName
          avatarUrl
        }
        role {
          id
          name
          description
          permissions {
            id
            area
            canRead
            canCreate
            canModify
            canDelete
            canReassign
          }
        }
      }
    }
  `;
};

export const buildInviteBoardMemberMutation = (
  boardId: string,
  user: string,
  roleId: string
): string => {
  return /* GraphQL */ `
    mutation {
      inviteBoardMember(
        boardId: ${serializeGraphQLString(boardId)}
        user: ${serializeGraphQLString(user)}
        roleId: ${roleId}
      ) {
        boardId
        userId
        createdAt
        user {
          id
          firstName
          lastName
          avatarUrl
        }
        role {
          id
          name
          description
          permissions {
            id
            area
            canRead
            canCreate
            canModify
            canDelete
            canReassign
          }
        }
      }
    }
  `;
};

export const buildChangeBoardMemberRoleMutation = (
  boardId: string,
  userId: string,
  roleId: string
): string => {
  return /* GraphQL */ `
    mutation {
      changeBoardMemberRole(
        boardId: ${serializeGraphQLString(boardId)}
        userId: ${serializeGraphQLString(userId)}
        roleId: ${roleId}
      ) {
        boardId
        userId
        createdAt
        user {
          id
          firstName
          lastName
          avatarUrl
        }
        role {
          id
          name
          description
          permissions {
            id
            area
            canRead
            canCreate
            canModify
            canDelete
            canReassign
          }
        }
      }
    }
  `;
};

export const buildRemoveBoardMemberMutation = (
  boardId: string,
  userId: string
): string => {
  return /* GraphQL */ `
    mutation {
      removeBoardMember(
        boardId: ${serializeGraphQLString(boardId)}
        userId: ${serializeGraphQLString(userId)}
      )
    }
  `;
};

export const buildCreateBoardMutation = (
  request: CreateBoardRequest
): string => {
  return /* GraphQL */ `
    mutation {
      createBoard(
        name: ${serializeGraphQLString(request.name)}
        description: ${serializeNullableGraphQLString(request.description)}
      ) {
        ${BOARD_FIELDS}
      }
    }
  `;
};

export const buildChangeBoardTitleMutation = (
  boardId: string,
  title: string
): string => {
  return /* GraphQL */ `
    mutation {
      changeBoardTitle(
        boardId: ${serializeGraphQLString(boardId)}
        name: ${serializeGraphQLString(title)}
      ) {
        ${BOARD_FIELDS}
      }
    }
  `;
};

export const buildChangeBoardDescriptionMutation = (
  boardId: string,
  description: string | null
): string => {
  return /* GraphQL */ `
    mutation {
      changeBoardDescription(
        boardId: ${serializeGraphQLString(boardId)}
        description: ${
          description === null
            ? "null"
            : serializeGraphQLString(description)
        }
      ) {
        ${BOARD_FIELDS}
      }
    }
  `;
};

export const buildChangeBoardOwnershipMutation = (
  boardId: string,
  owner: string
): string => {
  return /* GraphQL */ `
    mutation {
      changeBoardOwnership(
        boardId: ${serializeGraphQLString(boardId)}
        owner: ${serializeGraphQLString(owner)}
      ) {
        ${BOARD_FIELDS}
      }
    }
  `;
};

export const buildDeactivateBoardMutation = (boardId: string): string => {
  return /* GraphQL */ `
    mutation {
      deactivateBoard(boardId: ${serializeGraphQLString(boardId)}) {
        ${BOARD_FIELDS}
      }
    }
  `;
};

export const buildActivateBoardMutation = (boardId: string): string => {
  return /* GraphQL */ `
    mutation {
      activateBoard(boardId: ${serializeGraphQLString(boardId)}) {
        ${BOARD_FIELDS}
      }
    }
  `;
};
