import { createContext } from "react";

interface CurrentUser {
  userId: string;
}

export interface CurrentUserContextValue {
  currentUser: CurrentUser | null;
  userId: string | null;
}

export const CurrentUserContext = createContext<CurrentUserContextValue>({
  currentUser: null,
  userId: null
});
