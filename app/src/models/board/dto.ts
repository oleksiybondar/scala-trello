export interface BoardUserSummaryResponse {
  avatarUrl: string | null;
  firstName: string;
  id: string;
  lastName: string;
}

export interface BoardRoleResponse {
  description: string | null;
  id: string;
  name: string;
}

export interface BoardMemberResponse {
  boardId: string;
  createdAt: string;
  role: BoardRoleResponse;
  user: BoardUserSummaryResponse | null;
  userId: string;
}

export interface BoardResponse {
  active: boolean;
  createdAt: string;
  createdBy: BoardUserSummaryResponse | null;
  createdByUserId: string;
  description: string | null;
  id: string;
  lastModifiedByUserId: string;
  membersCount: number;
  modifiedAt: string;
  name: string;
  owner: BoardUserSummaryResponse | null;
  ownerUserId: string;
}

export interface CreateBoardRequest {
  description?: string;
  name: string;
}

export interface BoardQueryResponse {
  board: BoardResponse | null;
}

export interface MyBoardsQueryResponse {
  myBoards: BoardResponse[];
}

export interface BoardMembersQueryResponse {
  dashboardMembers: BoardMemberResponse[];
}

export interface CreateBoardMutationResponse {
  createBoard: BoardResponse;
}

export interface ChangeBoardTitleMutationResponse {
  changeBoardTitle: BoardResponse;
}

export interface ChangeBoardDescriptionMutationResponse {
  changeBoardDescription: BoardResponse;
}

export interface ChangeBoardOwnershipMutationResponse {
  changeBoardOwnership: BoardResponse;
}

export interface DeactivateBoardMutationResponse {
  deactivateBoard: BoardResponse;
}

export interface ActivateBoardMutationResponse {
  activateBoard: BoardResponse;
}
