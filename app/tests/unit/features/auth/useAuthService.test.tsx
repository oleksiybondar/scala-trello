import { renderHook } from "@testing-library/react";

import type { AuthTokenResponse } from "@features/auth/types";
import type { AuthSession } from "@features/auth/types";
import type { AuthStateApi } from "@features/auth/useAuthState";
import { useAuthService } from "@features/auth/useAuthService";

vi.mock("@features/auth/authApi", () => ({
  loginRequest: vi.fn(),
  logoutRequest: vi.fn(),
  refreshRequest: vi.fn()
}));

import {
  loginRequest,
  logoutRequest,
  refreshRequest
} from "@features/auth/authApi";

const createAuthState = (
  overrides: Partial<AuthStateApi> = {}
): AuthStateApi => {
  const session: AuthSession = {
    accessToken: "access-1",
    expiresAt: Date.now() + 300_000,
    expiresIn: 300,
    refreshToken: "refresh-1",
    tokenType: "Bearer"
  };

  return {
    accessToken: session.accessToken,
    applySession: vi.fn(),
    clearRefreshTimeout: vi.fn(),
    clearSession: vi.fn(),
    isAuthenticated: true,
    refreshTimeoutIdRef: { current: null },
    session,
    setStatus: vi.fn(),
    status: "authenticated",
    ...overrides
  };
};

describe("useAuthService", () => {
  const createPromiseRefs = () => ({
    loginPromiseRef: { current: null as Promise<void> | null },
    refreshPromiseRef: { current: null as Promise<void> | null }
  });

  afterEach(() => {
    vi.clearAllMocks();
    vi.restoreAllMocks();
  });

  test("logs in and applies the returned session", async () => {
    const authState = createAuthState({
      accessToken: null,
      isAuthenticated: false,
      session: null,
      status: "anonymous"
    });

    vi.mocked(loginRequest).mockResolvedValueOnce({
      access_token: "access-2",
      refresh_token: "refresh-2",
      token_type: "Bearer",
      expires_in: 3600
    });

    const { result } = renderHook(() =>
      useAuthService({
        authState,
        ...createPromiseRefs()
      })
    );

    await result.current.login({
      login: "demo",
      password: "secret"
    });

    expect(authState.setStatus).toHaveBeenCalledWith("authenticating");
    expect(loginRequest).toHaveBeenCalledWith({
      login: "demo",
      password: "secret"
    });
    expect(authState.applySession).toHaveBeenCalledWith({
      access_token: "access-2",
      refresh_token: "refresh-2",
      token_type: "Bearer",
      expires_in: 3600
    });
  });

  test("deduplicates concurrent refresh calls", async () => {
    let resolveRefresh!: (value: AuthTokenResponse) => void;
    const authState = createAuthState();
    const { loginPromiseRef, refreshPromiseRef } = createPromiseRefs();

    vi.mocked(refreshRequest).mockReturnValue(
      new Promise<AuthTokenResponse>(resolve => {
        resolveRefresh = resolve;
      })
    );

    const { result } = renderHook(() =>
      useAuthService({
        authState,
        loginPromiseRef,
        refreshPromiseRef
      })
    );

    const firstRefresh = result.current.refreshSession();
    const secondRefresh = result.current.refreshSession();

    resolveRefresh({
      access_token: "access-2",
      refresh_token: "refresh-2",
      token_type: "Bearer",
      expires_in: 3600
    });

    await Promise.all([firstRefresh, secondRefresh]);

    expect(refreshRequest).toHaveBeenCalledTimes(1);
    expect(authState.applySession).toHaveBeenCalledTimes(1);
  });

  test("clears the session when refresh fails", async () => {
    const authState = createAuthState();

    vi.mocked(refreshRequest).mockRejectedValueOnce(new Error("refresh failed"));

    const { result } = renderHook(() =>
      useAuthService({
        authState,
        ...createPromiseRefs()
      })
    );

    await expect(result.current.refreshSession()).rejects.toThrow("refresh failed");
    expect(authState.clearSession).toHaveBeenCalled();
  });

  test("clears local state before logout request", async () => {
    const authState = createAuthState();

    vi.mocked(logoutRequest).mockResolvedValueOnce();

    const { result } = renderHook(() =>
      useAuthService({
        authState,
        ...createPromiseRefs()
      })
    );

    await result.current.logout();

    expect(authState.clearSession).toHaveBeenCalledBefore(logoutRequest as never);
    expect(logoutRequest).toHaveBeenCalledWith("refresh-1");
  });

  test("schedules automatic refresh when a session exists", async () => {
    const authState = createAuthState();
    const setTimeoutSpy = vi
      .spyOn(window, "setTimeout")
      .mockImplementation(handler => {
        if (typeof handler !== "function") {
          throw new Error("Expected the scheduled refresh handler to be a function.");
        }

        return 1 as unknown as ReturnType<typeof window.setTimeout>;
      });

    renderHook(() =>
      useAuthService({
        authState,
        ...createPromiseRefs()
      })
    );

    expect(setTimeoutSpy).toHaveBeenCalledTimes(1);
    expect(typeof setTimeoutSpy.mock.calls[0]?.[0]).toBe("function");
    expect(authState.refreshTimeoutIdRef.current).toBe(1);
  });
});
