export interface BoardUserSummary {
  avatarUrl: string | null;
  firstName: string;
  lastName: string;
  userId: string;
}

export interface Board {
  active: boolean;
  boardId: string;
  createdAt: string;
  createdBy: BoardUserSummary | null;
  createdByUserId: string;
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

export interface BoardRole {
  description: string | null;
  roleId: string;
  roleName: string;
}

export interface BoardMember {
  boardId: string;
  createdAt: string;
  role: BoardRole;
  user: BoardUserSummary | null;
  userId: string;
}
