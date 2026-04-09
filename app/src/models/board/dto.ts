export interface BoardUserSummaryResponse {
  avatarUrl: string | null;
  firstName: string;
  id: string;
  lastName: string;
}

export interface DashboardResponse {
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

export interface MyDashboardsQueryResponse {
  myDashboards: DashboardResponse[];
}

export interface CreateDashboardMutationResponse {
  createDashboard: DashboardResponse;
}
