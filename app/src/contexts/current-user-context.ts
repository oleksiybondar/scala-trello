import { createContext } from "react";

import type { CurrentUser } from "@models/user";

export interface CurrentUserContextValue {
  currentUser: CurrentUser | null;
  userId: string | null;
}

export const CurrentUserContext = createContext<CurrentUserContextValue>({
  currentUser: null,
  userId: null
});
