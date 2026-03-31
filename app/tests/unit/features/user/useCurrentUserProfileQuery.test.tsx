import type { ReactElement } from "react";

import {
  QueryClient,
  QueryClientProvider
} from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { buildCurrentUserProfileQuery } from "@features/user/userQueries";
import { useCurrentUserProfileQuery } from "@features/user/useCurrentUserProfileQuery";
import { useAuth } from "@hooks/useAuth";
import { AuthProvider } from "@providers/AuthProvider";
import { CurrentUserProvider } from "@providers/CurrentUserProvider";

const createJwt = (payload: object): string => {
  const encode = (value: string): string => {
    return btoa(value).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/u, "");
  };

  return [
    encode(JSON.stringify({ alg: "none", typ: "JWT" })),
    encode(JSON.stringify(payload)),
    "signature"
  ].join(".");
};

const QueryConsumer = (): ReactElement => {
  const { login } = useAuth();
  const currentUserProfileQuery = useCurrentUserProfileQuery();

  return (
    <div>
      <div>Status: {currentUserProfileQuery.status}</div>
      <div>Display name: {currentUserProfileQuery.data?.displayName ?? "none"}</div>
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

const renderQueryConsumer = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false
      }
    }
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <CurrentUserProvider>
          <QueryConsumer />
        </CurrentUserProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
};

describe("useCurrentUserProfileQuery", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  test("loads the current user profile via GraphQL and maps the display name", async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn<typeof fetch>();

    fetchMock
      .mockResolvedValueOnce(
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
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            data: {
              user: {
                firstName: "Ada",
                lastName: "Lovelace"
              }
            }
          }),
          {
            status: 200
          }
        )
      );

    vi.stubGlobal("fetch", fetchMock);

    renderQueryConsumer();

    await user.click(screen.getByRole("button", { name: "Login" }));

    expect(await screen.findByText("Display name: Ada Lovelace")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenLastCalledWith(
      "/graphql",
      expect.objectContaining({
        body: JSON.stringify({
          query: buildCurrentUserProfileQuery("user-123")
        }),
        credentials: "include",
        headers: expect.objectContaining({
          Authorization: expect.stringContaining("Bearer ")
        }),
        method: "POST"
      })
    );
  });
});
