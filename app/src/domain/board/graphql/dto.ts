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

export interface BoardTimeTrackingTicketSummaryResponse {
  description: string | null;
  id: string;
  title: string;
}

export interface BoardTimeTrackingEntryResponse {
  activityCode: string | null;
  activityId: string;
  activityName: string | null;
  description: string | null;
  durationMinutes: number;
  id: string;
  loggedAt: string;
  ticket: BoardTimeTrackingTicketSummaryResponse | null;
  ticketId: string;
  user: BoardUserSummaryResponse | null;
  userId: string;
}

export interface BoardTicketResponse {
  acceptanceCriteria: string | null;
  assignedTo: BoardUserSummaryResponse | null;
  assignedToUserId: string | null;
  boardId: string;
  commentsCount: number;
  createdAt: string;
  createdBy: BoardUserSummaryResponse | null;
  createdByUserId: string;
  description: string | null;
  estimatedMinutes: number | null;
  id: string;
  lastModifiedBy: BoardUserSummaryResponse | null;
  lastModifiedByUserId: string;
  modifiedAt: string;
  name: string;
  status: string | null;
  timeEntries: BoardTimeTrackingEntryResponse[];
  trackedMinutes: number;
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
  tickets: BoardTicketResponse[];
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
