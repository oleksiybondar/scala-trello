export interface RoleResponse {
  description: string | null;
  id: string;
  name: string;
}

export interface RolesQueryResponse {
  roles: RoleResponse[];
}
