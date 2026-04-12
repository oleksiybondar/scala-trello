import type { RoleResponse } from "./dto";
import type { Role } from "./types";

export const mapRoleResponseToRole = (response: RoleResponse): Role => {
  return {
    description: response.description,
    roleId: response.id,
    roleName: response.name
  };
};
