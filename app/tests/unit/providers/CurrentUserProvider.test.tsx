import type { ReactElement } from "react";

import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { useAuth } from "@hooks/useAuth";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { AuthProvider } from "@providers/AuthProvider";
import { CurrentUserProvider } from "@providers/CurrentUserProvider";

const CurrentUserConsumer = (): ReactElement => {
  const { login, logout } = useAuth();
  const { currentUser, userId } = useCurrentUser();

  return (
    <div>
      <div>User id: {userId ?? "none"}</div>
      <div>Current user: {currentUser?.userId ?? "none"}</div>
      <div>First name: {currentUser?.firstName ?? "none"}</div>
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
          void logout();
        }}
        type="button"
      >
        Logout
      </button>
    </div>
  );
};

describe("CurrentUserProvider", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  test("loads the current user from auth/me after login", async () => {
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
            id: "user-123",
            username: "demo",
            email: "demo@example.com",
            first_name: "Demo",
            last_name: "User",
            avatar_url: null,
            created_at: "2026-03-25T10:15:30Z"
          }),
          {
            status: 200
          }
        )
      );

    vi.stubGlobal("fetch", fetchMock);

    render(
      <AuthProvider>
        <CurrentUserProvider>
          <CurrentUserConsumer />
        </CurrentUserProvider>
      </AuthProvider>
    );

    await user.click(screen.getByRole("button", { name: "Login" }));

    await screen.findByText("User id: user-123");
    expect(screen.getByText("Current user: user-123")).toBeInTheDocument();
    expect(screen.getByText("First name: Demo")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      "/auth/me",
      expect.objectContaining({
        credentials: "include",
        headers: {
          Authorization: "Bearer access-1"
        },
        method: "GET"
      })
    );
  });

  test("clears the current user when auth/me fails", async () => {
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
          status: 401
        })
      );

    vi.stubGlobal("fetch", fetchMock);

    render(
      <AuthProvider>
        <CurrentUserProvider>
          <CurrentUserConsumer />
        </CurrentUserProvider>
      </AuthProvider>
    );

    await user.click(screen.getByRole("button", { name: "Login" }));

    await screen.findByText("User id: none");
    expect(screen.getByText("Current user: none")).toBeInTheDocument();
    expect(screen.getByText("First name: none")).toBeInTheDocument();
  });

  test("clears the current user after logout removes the auth token", async () => {
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
            id: "user-123",
            username: "demo",
            email: "demo@example.com",
            first_name: "Demo",
            last_name: "User",
            avatar_url: null,
            created_at: "2026-03-25T10:15:30Z"
          }),
          {
            status: 200
          }
        )
      )
      .mockResolvedValueOnce(
        new Response(null, {
          status: 204
        })
      );

    vi.stubGlobal("fetch", fetchMock);

    render(
      <AuthProvider>
        <CurrentUserProvider>
          <CurrentUserConsumer />
        </CurrentUserProvider>
      </AuthProvider>
    );

    await user.click(screen.getByRole("button", { name: "Login" }));
    await screen.findByText("User id: user-123");

    await user.click(screen.getByRole("button", { name: "Logout" }));

    await screen.findByText("User id: none");
    expect(screen.getByText("Current user: none")).toBeInTheDocument();
  });
});
