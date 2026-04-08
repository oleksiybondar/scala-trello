export interface DashboardResponse {
  active: boolean;
  createdAt: string;
  createdByUserId: string;
  description: string | null;
  id: string;
  lastModifiedByUserId: string;
  modifiedAt: string;
  name: string;
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
