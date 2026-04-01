import { createContext } from "react";
import type {
  AuthSession,
  AuthStatus,
  LoginCredentials,
  RegisterCredentials
} from "@features/auth/types";

/**
 * Shared auth contract exposed to React consumers.
 */
export interface AuthContextValue {
  /** Current access token or `null` when the user is anonymous. */
  accessToken: string | null;
  /** Convenience flag derived from the current session state. */
  isAuthenticated: boolean;
  /** Authenticates the user and creates a new local session. */
  login: (credentials: LoginCredentials) => Promise<void>;
  /** Registers a new user and creates a new local session. */
  register: (credentials: RegisterCredentials) => Promise<void>;
  /** Clears the local session and attempts to invalidate it on the backend. */
  logout: () => Promise<void>;
  /** Renews the current session using the active refresh token. */
  refreshSession: () => Promise<void>;
  /** Full in-memory auth session or `null` when no session exists. */
  session: AuthSession | null;
  /** Current authentication lifecycle state. */
  status: AuthStatus;
}

const missingAuthProvider = (): never => {
  throw new Error("AuthContext is missing its provider.");
};

/**
 * React context that carries app-wide auth state and auth actions.
 */
export const AuthContext = createContext<AuthContextValue>({
  accessToken: null,
  isAuthenticated: false,
  login: missingAuthProvider,
  register: missingAuthProvider,
  logout: missingAuthProvider,
  refreshSession: missingAuthProvider,
  session: null,
  status: "anonymous"
});
