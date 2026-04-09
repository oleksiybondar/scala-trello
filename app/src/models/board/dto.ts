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
  permissions: BoardPermissionResponse[];
}

export interface BoardPermissionResponse {
  area: string;
  canCreate: boolean;
  canDelete: boolean;
  canModify: boolean;
  canRead: boolean;
  canReassign: boolean;
  id: string;
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
  currentUserRole: BoardRoleResponse | null;
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
  boardMembers: BoardMemberResponse[];
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

export interface InviteBoardMemberMutationResponse {
  inviteBoardMember: BoardMemberResponse;
}

export interface ChangeBoardMemberRoleMutationResponse {
  changeBoardMemberRole: BoardMemberResponse;
}

export interface RemoveBoardMemberMutationResponse {
  removeBoardMember: boolean;
}
