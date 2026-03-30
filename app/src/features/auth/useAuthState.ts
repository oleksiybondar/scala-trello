import type {
  Dispatch,
  RefObject,
  SetStateAction
} from "react";

import { toAuthSession } from "@features/auth/authHelpers";
import type {
  AuthSession,
  AuthStatus,
  AuthTokenResponse
} from "@features/auth/types";

interface UseAuthStateParameters {
  /** Mutable reference holding the currently scheduled refresh timeout id. */
  refreshTimeoutIdRef: RefObject<number | null>;
  /** Current in-memory auth session. */
  session: AuthSession | null;
  /** React state setter for the current session. */
  setSession: Dispatch<SetStateAction<AuthSession | null>>;
  /** React state setter for the auth lifecycle status. */
  setStatus: Dispatch<SetStateAction<AuthStatus>>;
  /** Current auth lifecycle status. */
  status: AuthStatus;
}

/**
 * State and mutation helpers shared between the provider and auth service.
 */
export interface AuthStateApi {
  /** Current access token or `null` when no session exists. */
  accessToken: string | null;
  /** Applies a backend token response as the new authenticated session. */
  applySession: (tokenResponse: AuthTokenResponse) => void;
  /** Cancels any scheduled proactive token refresh. */
  clearRefreshTimeout: () => void;
  /** Clears the current session and resets the auth status to anonymous. */
  clearSession: () => void;
  /** Convenience flag derived from the current session state. */
  isAuthenticated: boolean;
  /** Mutable reference holding the currently scheduled refresh timeout id. */
  refreshTimeoutIdRef: RefObject<number | null>;
  /** Current in-memory auth session. */
  session: AuthSession | null;
  /** React state setter for the auth lifecycle status. */
  setStatus: Dispatch<SetStateAction<AuthStatus>>;
  /** Current auth lifecycle status. */
  status: AuthStatus;
}

/**
 * Wraps raw auth state setters in intent-based helpers for session transitions.
 *
 * @param params Raw auth state and setters owned by the provider.
 * @returns State-derived values plus helper methods for auth transitions.
 */
export const useAuthState = ({
  refreshTimeoutIdRef,
  session,
  setSession,
  setStatus,
  status
}: UseAuthStateParameters): AuthStateApi => {
  const clearRefreshTimeout = (): void => {
    if (refreshTimeoutIdRef.current !== null) {
      window.clearTimeout(refreshTimeoutIdRef.current);
      refreshTimeoutIdRef.current = null;
    }
  };

  const clearSession = (): void => {
    clearRefreshTimeout();
    setSession(null);
    setStatus("anonymous");
  };

  const applySession = (tokenResponse: AuthTokenResponse): void => {
    clearRefreshTimeout();
    setSession(toAuthSession(tokenResponse));
    setStatus("authenticated");
  };

  return {
    accessToken: session?.accessToken ?? null,
    applySession,
    clearRefreshTimeout,
    clearSession,
    isAuthenticated: session !== null,
    refreshTimeoutIdRef,
    session,
    setStatus,
    status
  };
};
