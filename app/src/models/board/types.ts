export interface BoardUserSummary {
  avatarUrl: string | null;
  firstName: string;
  lastName: string;
  userId: string;
}

export interface BoardPermission {
  area: string;
  canCreate: boolean;
  canDelete: boolean;
  canModify: boolean;
  canRead: boolean;
  canReassign: boolean;
}

export interface BoardRole {
  description: string | null;
  permissions: BoardPermission[];
  roleId: string;
  roleName: string;
}

export interface Board {
  active: boolean;
  boardId: string;
  createdAt: string;
  createdBy: BoardUserSummary | null;
  createdByUserId: string;
  currentUserRole: BoardRole | null;
  description: string | null;
  lastModifiedByUserId: string;
  membersCount: number;
  modifiedAt: string;
  name: string;
  owner: BoardUserSummary | null;
  ownerUserId: string;
}

export interface CreateBoardInput {
  description: string | null;
  name: string;
}

export interface BoardMember {
  boardId: string;
  createdAt: string;
  role: BoardRole;
  user: BoardUserSummary | null;
  userId: string;
}
