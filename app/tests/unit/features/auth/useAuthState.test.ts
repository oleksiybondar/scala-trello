import { useAuthState } from "../../../../src/domain/auth/useAuthState";
import type { AuthSession } from "../../../../src/domain/auth/types";

describe("useAuthState", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  test("derives public auth state from the current session", () => {
    const session: AuthSession = {
      accessToken: "access-1",
      expiresAt: 1000,
      expiresIn: 300,
      refreshToken: "refresh-1",
      tokenType: "Bearer"
    };
    const setSession = vi.fn();
    const setStatus = vi.fn();
    const refreshTimeoutIdRef = { current: null };

    const authState = useAuthState({
      refreshTimeoutIdRef,
      session,
      setSession,
      setStatus,
      status: "authenticated"
    });

    expect(authState.accessToken).toBe("access-1");
    expect(authState.isAuthenticated).toBe(true);
    expect(authState.session).toBe(session);
    expect(authState.status).toBe("authenticated");
  });

  test("clears the timeout handle and resets session state", () => {
    const clearTimeoutSpy = vi.spyOn(window, "clearTimeout");
    const setSession = vi.fn();
    const setStatus = vi.fn();
    const refreshTimeoutIdRef = {
      current: 42 as unknown as ReturnType<typeof window.setTimeout>
    };

    const authState = useAuthState({
      refreshTimeoutIdRef: refreshTimeoutIdRef as unknown as {
        current: number | null;
      },
      session: null,
      setSession,
      setStatus,
      status: "refreshing"
    });

    authState.clearSession();

    expect(clearTimeoutSpy).toHaveBeenCalledWith(42);
    expect(refreshTimeoutIdRef.current).toBeNull();
    expect(setSession).toHaveBeenCalledWith(null);
    expect(setStatus).toHaveBeenCalledWith("anonymous");
  });

  test("applies a token response as an authenticated session", () => {
    const setSession = vi.fn();
    const setStatus = vi.fn();
    const refreshTimeoutIdRef = { current: null };

    const authState = useAuthState({
      refreshTimeoutIdRef,
      session: null,
      setSession,
      setStatus,
      status: "anonymous"
    });

    authState.applySession({
      access_token: "access-1",
      refresh_token: "refresh-1",
      token_type: "Bearer",
      expires_in: 300
    });

    expect(setSession).toHaveBeenCalledWith(
      expect.objectContaining({
        accessToken: "access-1",
        expiresIn: 300,
        refreshToken: "refresh-1",
        tokenType: "Bearer"
      })
    );
    expect(setStatus).toHaveBeenCalledWith("authenticated");
  });
});
