import { createContext } from "react";

import type { CurrentUser } from "@models/user";

export interface CurrentUserContextValue {
  currentUser: CurrentUser | null;
  refreshCurrentUser: () => Promise<void>;
  setCurrentUser: (user: CurrentUser | null) => void;
  userId: string | null;
}

export const CurrentUserContext = createContext<CurrentUserContextValue>({
  currentUser: null,
  refreshCurrentUser: () => Promise.resolve(),
  setCurrentUser: () => undefined,
  userId: null
});
