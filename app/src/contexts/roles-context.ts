import { createContext } from "react";

import type { Role } from "../domain/role/graphql";

export interface RolesContextValue {
  isLoadingRoles: boolean;
  roles: Role[];
  rolesError: Error | null;
}

export const RolesContext = createContext<RolesContextValue>({
  isLoadingRoles: false,
  roles: [],
  rolesError: null
});
