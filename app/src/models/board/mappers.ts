import type { CreateBoardRequest, DashboardResponse } from "@models/board/dto";
import type { Board, CreateBoardInput } from "@models/board/types";

export const mapDashboardResponseToBoard = (
  response: DashboardResponse
): Board => {
  return {
    active: response.active,
    boardId: response.id,
    createdAt: response.createdAt,
    createdByUserId: response.createdByUserId,
    description: response.description,
    lastModifiedByUserId: response.lastModifiedByUserId,
    modifiedAt: response.modifiedAt,
    name: response.name,
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
