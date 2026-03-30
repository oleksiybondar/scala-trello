import type { ReactElement } from "react";

import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { useAuth } from "@hooks/useAuth";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { AuthProvider } from "@providers/AuthProvider";
import { CurrentUserProvider } from "@providers/CurrentUserProvider";

const createJwt = (payload: object): string => {
  const encode = (value: string): string => {
    return btoa(value).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/u, "");
  };

  return [encode(JSON.stringify({ alg: "none", typ: "JWT" })), encode(JSON.stringify(payload)), "signature"].join(".");
};

const CurrentUserConsumer = (): ReactElement => {
  const { login } = useAuth();
  const { currentUser, userId } = useCurrentUser();

  return (
    <div>
      <div>User id: {userId ?? "none"}</div>
      <div>Current user: {currentUser?.userId ?? "none"}</div>
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
    </div>
  );
};

describe("CurrentUserProvider", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  test("derives the current user id from the authenticated access token", async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn<typeof fetch>();

    fetchMock.mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: createJwt({ sub: "user-123" }),
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
        <CurrentUserProvider>
          <CurrentUserConsumer />
        </CurrentUserProvider>
      </AuthProvider>
    );

    await user.click(screen.getByRole("button", { name: "Login" }));

    await screen.findByText("User id: user-123");
    expect(screen.getByText("Current user: user-123")).toBeInTheDocument();
  });

  test("exposes no current user when the authenticated token cannot be decoded", async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn<typeof fetch>();

    fetchMock.mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: "invalid-token",
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
        <CurrentUserProvider>
          <CurrentUserConsumer />
        </CurrentUserProvider>
      </AuthProvider>
    );

    await user.click(screen.getByRole("button", { name: "Login" }));

    await screen.findByText("User id: none");
    expect(screen.getByText("Current user: none")).toBeInTheDocument();
  });
});
