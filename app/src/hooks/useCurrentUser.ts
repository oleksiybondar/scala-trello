import { useContext } from "react";

import { CurrentUserContext } from "@contexts/current-user-context";
import type { CurrentUserContextValue } from "@contexts/current-user-context";

export const useCurrentUser = (): CurrentUserContextValue => {
  return useContext(CurrentUserContext);
};
