export interface Board {
  active: boolean;
  boardId: string;
  createdAt: string;
  createdByUserId: string;
  description: string | null;
  lastModifiedByUserId: string;
  modifiedAt: string;
  name: string;
  ownerUserId: string;
}

export interface CreateBoardInput {
  description: string | null;
  name: string;
}
