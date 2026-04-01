import type { RefObject } from "react";
import { useEffect } from "react";

import {
  loginRequest,
  logoutRequest,
  registerRequest,
  refreshRequest
} from "@features/auth/authApi";
import { getRefreshDelay } from "@features/auth/authHelpers";
import type {
  LoginCredentials,
  RegisterCredentials
} from "@features/auth/types";
import type { AuthStateApi } from "@features/auth/useAuthState";

interface UseAuthServiceParameters {
  /** State accessors and mutation helpers supplied by the auth provider. */
  authState: AuthStateApi;
  /** Reference used to deduplicate in-flight login requests. */
  loginPromiseRef: RefObject<Promise<void> | null>;
  /** Reference used to deduplicate in-flight refresh requests. */
  refreshPromiseRef: RefObject<Promise<void> | null>;
}

/**
 * Auth operations exposed by the feature service layer.
 */
interface AuthServiceApi {
  /** Authenticates the user and stores the resulting session. */
  login: (credentials: LoginCredentials) => Promise<void>;
  /** Registers the user and stores the resulting session. */
  register: (credentials: RegisterCredentials) => Promise<void>;
  /** Clears the local session and revokes it on the backend when possible. */
  logout: () => Promise<void>;
  /** Renews the current session using the active refresh token. */
  refreshSession: () => Promise<void>;
}

/**
 * Coordinates auth API calls with local auth state transitions and timers.
 *
 * @param params State helpers and refs needed to orchestrate auth behavior.
 * @returns Service methods that operate on the provided auth state.
 */
export const useAuthService = ({
  authState,
  loginPromiseRef,
  refreshPromiseRef
}: UseAuthServiceParameters): AuthServiceApi => {
  const refreshWithToken = async (refreshToken: string): Promise<void> => {
    if (refreshPromiseRef.current !== null) {
      return refreshPromiseRef.current;
    }

    authState.setStatus(currentStatus => {
      return currentStatus === "anonymous" ? currentStatus : "refreshing";
    });

    const refreshPromise = (async () => {
      try {
        const nextTokens = await refreshRequest(refreshToken);

        authState.applySession(nextTokens);
      } catch (error) {
        authState.clearSession();
        throw error;
      } finally {
        refreshPromiseRef.current = null;
      }
    })();

    refreshPromiseRef.current = refreshPromise;

    return refreshPromise;
  };

  useEffect(() => {
    authState.clearRefreshTimeout();

    if (authState.session === null) {
      return;
    }

    const delay = getRefreshDelay(authState.session);

    const nextRefreshToken = authState.session.refreshToken;

    authState.refreshTimeoutIdRef.current = window.setTimeout(() => {
      void refreshWithToken(nextRefreshToken).catch(() => undefined);
    }, delay);

    return () => {
      authState.clearRefreshTimeout();
    };
  }, [authState]);

  useEffect(() => {
    return () => {
      authState.clearRefreshTimeout();
    };
  }, [authState]);

  const login = async (credentials: LoginCredentials): Promise<void> => {
    if (authState.session !== null) {
      return;
    }

    if (loginPromiseRef.current !== null) {
      return loginPromiseRef.current;
    }

    authState.setStatus("authenticating");

    const loginPromise = (async () => {
      try {
        const tokenResponse = await loginRequest(credentials);

        authState.applySession(tokenResponse);
      } catch (error) {
        authState.clearSession();
        throw error;
      } finally {
        loginPromiseRef.current = null;
      }
    })();

    loginPromiseRef.current = loginPromise;

    return loginPromise;
  };

  const refreshSession = async (): Promise<void> => {
    if (authState.session === null) {
      return;
    }

    await refreshWithToken(authState.session.refreshToken);
  };

  const register = async (credentials: RegisterCredentials): Promise<void> => {
    if (authState.session !== null) {
      return;
    }

    if (loginPromiseRef.current !== null) {
      return loginPromiseRef.current;
    }

    authState.setStatus("authenticating");

    const registerPromise = (async () => {
      try {
        const tokenResponse = await registerRequest(credentials);

        authState.applySession(tokenResponse);
      } catch (error) {
        authState.clearSession();
        throw error;
      } finally {
        loginPromiseRef.current = null;
      }
    })();

    loginPromiseRef.current = registerPromise;

    return registerPromise;
  };

  const logout = async (): Promise<void> => {
    const refreshToken = authState.session?.refreshToken ?? null;

    authState.clearSession();

    if (refreshToken === null) {
      return;
    }

    try {
      await logoutRequest(refreshToken);
    } catch {
      return;
    }
  };

  return {
    login,
    logout,
    register,
    refreshSession
  };
};
