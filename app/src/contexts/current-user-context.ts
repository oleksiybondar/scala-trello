import { createContext } from 'react';

interface CurrentUser {
  userId: string;
  username: string | null;
  email: string | null;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
  createdAt: string;
}

export interface CurrentUserContextValue {
  currentUser: CurrentUser | null;
  userId: string | null;
}

export const CurrentUserContext = createContext<CurrentUserContextValue>({
  currentUser: null,
  userId: null
});
