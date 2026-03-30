import type { PropsWithChildren, ReactElement } from "react";
import { useRef, useState } from "react";

import { AuthContext } from "@contexts/auth-context";
import type { AuthStatus, AuthSession } from "@features/auth/types";
import { useAuthState } from "@features/auth/useAuthState";
import { useAuthService } from "@features/auth/useAuthService";

/**
 * Composes auth state and auth service methods into the root auth context.
 *
 * @param children Descendant React nodes that should receive auth context.
 * @returns The auth provider wrapping the given children.
 */
export const AuthProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [status, setStatus] = useState<AuthStatus>("anonymous");
  const loginPromiseRef = useRef<Promise<void> | null>(null);
  const refreshTimeoutIdRef = useRef<number | null>(null);
  const refreshPromiseRef = useRef<Promise<void> | null>(null);

  const authState = useAuthState({
    refreshTimeoutIdRef,
    session,
    setSession,
    setStatus,
    status
  });
  const { login, logout, refreshSession } = useAuthService({
    authState,
    loginPromiseRef,
    refreshPromiseRef
  });

  return (
    <AuthContext.Provider
      value={{
        accessToken: authState.accessToken,
        isAuthenticated: authState.isAuthenticated,
        login,
        logout,
        refreshSession,
        session: authState.session,
        status: authState.status
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
