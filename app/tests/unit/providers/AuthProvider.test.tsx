import type { ReactElement } from "react";

import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { useAuth } from "@hooks/useAuth";
import { AuthProvider } from "@providers/AuthProvider";

const AuthConsumer = (): ReactElement => {
  const { accessToken, isAuthenticated, login, logout, refreshSession, status } =
    useAuth();

  return (
    <div>
      <div>Status: {status}</div>
      <div>Access token: {accessToken ?? "none"}</div>
      <div>Authenticated: {String(isAuthenticated)}</div>
      <button
        onClick={() => {
          void login({
            login: "demo",
            password: "secret"
          });
        }}
        type="button"
      >
        Login
      </button>
      <button
        onClick={() => {
          void refreshSession();
        }}
        type="button"
      >
        Refresh
      </button>
      <button
        onClick={() => {
          void logout();
        }}
        type="button"
      >
        Logout
      </button>
    </div>
  );
};

describe("AuthProvider", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    vi.useRealTimers();
  });

  test("logs in and exposes the authenticated session", async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn<typeof fetch>();

    fetchMock.mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: "access-1",
          refresh_token: "refresh-1",
          token_type: "Bearer",
          expires_in: 3600
        }),
        {
          status: 200
        }
      )
    );

    vi.stubGlobal("fetch", fetchMock);

    render(
      <AuthProvider>
        <AuthConsumer />
      </AuthProvider>
    );

    await user.click(screen.getByRole("button", { name: "Login" }));

    expect(await screen.findByText("Status: authenticated")).toBeInTheDocument();
    expect(screen.getByText("Access token: access-1")).toBeInTheDocument();
    expect(screen.getByText("Authenticated: true")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith(
      "/auth/login",
      expect.objectContaining({
        body: JSON.stringify({
          login: "demo",
          password: "secret"
        }),
        credentials: "include",
        method: "POST"
      })
    );
  });

  test("deduplicates concurrent login attempts", async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn<typeof fetch>();
    let resolveLogin!: (value: Response) => void;

    const pendingLoginResponse = new Promise<Response>(resolve => {
      resolveLogin = resolve;
    });

    fetchMock.mockReturnValueOnce(pendingLoginResponse);

    vi.stubGlobal("fetch", fetchMock);

    render(
      <AuthProvider>
        <AuthConsumer />
      </AuthProvider>
    );

    const loginButton = screen.getByRole("button", { name: "Login" });

    await Promise.all([
      user.click(loginButton),
      user.click(loginButton)
    ]);

    expect(fetchMock).toHaveBeenCalledTimes(1);

    resolveLogin(
      new Response(
        JSON.stringify({
          access_token: "access-1",
          refresh_token: "refresh-1",
          token_type: "Bearer",
          expires_in: 3600
        }),
        {
          status: 200
        }
      )
    );

    expect(await screen.findByText("Status: authenticated")).toBeInTheDocument();
  });

  test("refreshes the active session", async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn<typeof fetch>();

    fetchMock
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            access_token: "access-1",
            refresh_token: "refresh-1",
            token_type: "Bearer",
            expires_in: 3600
          }),
          {
            status: 200
          }
        )
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            access_token: "access-2",
            refresh_token: "refresh-2",
            token_type: "Bearer",
            expires_in: 3600
          }),
          {
            status: 200
          }
        )
      );

    vi.stubGlobal("fetch", fetchMock);

    render(
      <AuthProvider>
        <AuthConsumer />
      </AuthProvider>
    );

    await user.click(screen.getByRole("button", { name: "Login" }));
    await user.click(screen.getByRole("button", { name: "Refresh" }));

    expect(await screen.findByText("Access token: access-2")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenLastCalledWith(
      "/auth/refresh",
      expect.objectContaining({
        body: JSON.stringify({
          refresh_token: "refresh-1"
        })
      })
    );
  });

  test("clears the local session even if logout fails", async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn<typeof fetch>();

    fetchMock
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            access_token: "access-1",
            refresh_token: "refresh-1",
            token_type: "Bearer",
            expires_in: 3600
          }),
          {
            status: 200
          }
        )
      )
      .mockResolvedValueOnce(
        new Response(null, {
          status: 500
        })
      );

    vi.stubGlobal("fetch", fetchMock);

    render(
      <AuthProvider>
        <AuthConsumer />
      </AuthProvider>
    );

    await user.click(screen.getByRole("button", { name: "Login" }));
    await user.click(screen.getByRole("button", { name: "Logout" }));

    await waitFor(() => {
      expect(screen.getByText("Status: anonymous")).toBeInTheDocument();
    });
    expect(screen.getByText("Access token: none")).toBeInTheDocument();
    expect(screen.getByText("Authenticated: false")).toBeInTheDocument();
  });

  test("refreshes automatically before token expiry", async () => {
    const fetchMock = vi.fn<typeof fetch>();
    let scheduledRefresh: (() => void) | null = null;

    vi.spyOn(window, "setTimeout").mockImplementation(handler => {
      if (typeof handler !== "function") {
        throw new Error("Expected the refresh timer handler to be a function.");
      }

      scheduledRefresh = handler;

      return 1 as unknown as ReturnType<typeof window.setTimeout>;
    });

    fetchMock
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            access_token: "access-1",
            refresh_token: "refresh-1",
            token_type: "Bearer",
            expires_in: 61
          }),
          {
            status: 200
          }
        )
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            access_token: "access-2",
            refresh_token: "refresh-2",
            token_type: "Bearer",
            expires_in: 3600
          }),
          {
            status: 200
          }
        )
      );

    vi.stubGlobal("fetch", fetchMock);

    render(
      <AuthProvider>
        <AuthConsumer />
      </AuthProvider>
    );

    await act(async () => {
      fireEvent.click(screen.getByRole("button", { name: "Login" }));
      await Promise.resolve();
    });

    expect(screen.getByText("Access token: access-1")).toBeInTheDocument();
    expect(scheduledRefresh).not.toBeNull();

    await act(async () => {
      scheduledRefresh?.();
      await Promise.resolve();
    });

    expect(screen.getByText("Access token: access-2")).toBeInTheDocument();
  });
});
