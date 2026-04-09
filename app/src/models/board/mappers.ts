import type { CreateBoardRequest, DashboardResponse } from "@models/board/dto";
import type {
  Board,
  BoardUserSummary,
  CreateBoardInput
} from "@models/board/types";

const mapBoardUserSummary = (
  response: DashboardResponse["owner"]
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

export const mapDashboardResponseToBoard = (
  response: DashboardResponse
): Board => {
  return {
    active: response.active,
    boardId: response.id,
    createdAt: response.createdAt,
    createdBy: mapBoardUserSummary(response.createdBy),
    createdByUserId: response.createdByUserId,
    description: response.description,
    lastModifiedByUserId: response.lastModifiedByUserId,
    membersCount: response.membersCount,
    modifiedAt: response.modifiedAt,
    name: response.name,
    owner: mapBoardUserSummary(response.owner),
    ownerUserId: response.ownerUserId
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
