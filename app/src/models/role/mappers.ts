import type { RoleResponse } from "@models/role/dto";
import type { Role } from "@models/role/types";

export const mapRoleResponseToRole = (response: RoleResponse): Role => {
  return {
    description: response.description,
    roleId: response.id,
    roleName: response.name
  };
};
