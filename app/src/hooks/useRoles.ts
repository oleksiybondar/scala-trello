import { useContext } from "react";

import { RolesContext } from "@contexts/roles-context";
import type { RolesContextValue } from "@contexts/roles-context";

export const useRoles = (): RolesContextValue => {
  return useContext(RolesContext);
};
