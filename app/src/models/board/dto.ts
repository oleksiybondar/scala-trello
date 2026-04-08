export interface DashboardResponse {
  active: boolean;
  created_at: string;
  created_by_user_id: string;
  description: string | null;
  id: string;
  last_modified_by_user_id: string;
  modified_at: string;
  name: string;
  owner_user_id: string;
}

export interface CreateBoardRequest {
  description?: string;
  name: string;
}
