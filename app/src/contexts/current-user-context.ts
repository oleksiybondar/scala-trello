import { createContext } from "react";

import type { CurrentUser } from "../domain/user/graphql";

export interface CurrentUserContextValue {
  changeEmail: (email: string) => Promise<void>;
  changePassword: (currentPassword: string, password: string) => Promise<void>;
  changeUsername: (username: string) => Promise<void>;
  currentUser: CurrentUser | null;
  refreshCurrentUser: () => Promise<void>;
  setCurrentUser: (user: CurrentUser | null) => void;
  updateAvatar: (avatarUrl: string | null) => Promise<void>;
  updateProfile: (firstName: string, lastName: string) => Promise<void>;
  userId: string | null;
}

export const CurrentUserContext = createContext<CurrentUserContextValue>({
  changeEmail: () => Promise.resolve(),
  changePassword: () => Promise.resolve(),
  changeUsername: () => Promise.resolve(),
  currentUser: null,
  refreshCurrentUser: () => Promise.resolve(),
  setCurrentUser: () => undefined,
  updateAvatar: () => Promise.resolve(),
  updateProfile: () => Promise.resolve(),
  userId: null
});
