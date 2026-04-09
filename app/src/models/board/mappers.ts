import type {
  BoardMemberResponse,
  BoardResponse,
  CreateBoardRequest
} from "@models/board/dto";
import type {
  Board,
  BoardMember,
  BoardPermission,
  BoardRole,
  BoardUserSummary,
  CreateBoardInput
} from "@models/board/types";

const mapBoardUserSummary = (
  response: BoardResponse["owner"]
): BoardUserSummary | null => {
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

const mapBoardPermission = (
  response: BoardMemberResponse["role"]["permissions"][number]
): BoardPermission => {
  return {
    area: response.area,
    canCreate: response.canCreate,
    canDelete: response.canDelete,
    canModify: response.canModify,
    canRead: response.canRead,
    canReassign: response.canReassign
  };
};

const mapBoardRole = (response: BoardResponse["currentUserRole"]): BoardRole | null => {
  if (response === null) {
    return null;
  }

  return {
    description: response.description,
    permissions: response.permissions.map(mapBoardPermission),
    roleId: response.id,
    roleName: response.name
  };
};

export const mapBoardResponseToBoard = (
  response: BoardResponse
): Board => {
  return {
    active: response.active,
    boardId: response.id,
    createdAt: response.createdAt,
    createdBy: mapBoardUserSummary(response.createdBy),
    createdByUserId: response.createdByUserId,
    currentUserRole: mapBoardRole(response.currentUserRole),
    description: response.description,
    lastModifiedByUserId: response.lastModifiedByUserId,
    membersCount: response.membersCount,
    modifiedAt: response.modifiedAt,
    name: response.name,
    owner: mapBoardUserSummary(response.owner),
    ownerUserId: response.ownerUserId
  };
};

export const mapBoardMemberResponseToBoardMember = (
  response: BoardMemberResponse
): BoardMember => {
  return {
    boardId: response.boardId,
    createdAt: response.createdAt,
    role: {
      description: response.role.description,
      permissions: response.role.permissions.map(mapBoardPermission),
      roleId: response.role.id,
      roleName: response.role.name
    },
    user: mapBoardUserSummary(response.user),
    userId: response.userId
  };
};

export const mapCreateBoardInputToRequest = (
  input: CreateBoardInput
): CreateBoardRequest => {
  const description = input.description?.trim() ?? "";

  return {
    ...(description.length > 0 ? { description } : {}),
    name: input.name.trim()
  };
};
