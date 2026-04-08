import type { CreateBoardRequest, DashboardResponse } from "@models/board/dto";
import type { Board, CreateBoardInput } from "@models/board/types";

export const mapDashboardResponseToBoard = (
  response: DashboardResponse
): Board => {
  return {
    active: response.active,
    boardId: response.id,
    createdAt: response.created_at,
    createdByUserId: response.created_by_user_id,
    description: response.description,
    lastModifiedByUserId: response.last_modified_by_user_id,
    modifiedAt: response.modified_at,
    name: response.name,
    ownerUserId: response.owner_user_id
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
