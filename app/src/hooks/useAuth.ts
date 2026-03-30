import { useContext } from "react";

import { AuthContext } from "@contexts/auth-context";
import type { AuthContextValue } from "@contexts/auth-context";

/**
 * Returns the current auth context value for the active React tree.
 *
 * @returns The auth state and auth actions exposed by {@link AuthContext}.
 */
export const useAuth = (): AuthContextValue => {
  return useContext(AuthContext);
};
