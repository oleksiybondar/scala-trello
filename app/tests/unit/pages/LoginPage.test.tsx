import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { LoginPage } from "@pages/LoginPage";
import { renderApp } from "@tests/setup/render";

describe("LoginPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  test("submits a single login request while authentication is in flight", async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn<typeof fetch>();
    let resolveLogin!: (value: Response) => void;

    const pendingLoginResponse = new Promise<Response>(resolve => {
      resolveLogin = resolve;
    });

    fetchMock.mockReturnValueOnce(pendingLoginResponse);

    vi.stubGlobal("fetch", fetchMock);

    renderApp(<LoginPage />);

    await user.type(screen.getByRole("textbox", { name: /login/i }), "demo");
    await user.type(screen.getByLabelText(/password/i), "secret");

    const submitButton = screen.getByRole("button", { name: "Sign in" });

    await Promise.all([
      user.click(submitButton),
      user.click(submitButton)
    ]);

    await waitFor(() => {
      expect(screen.getByRole("button", { name: "Signing in..." })).toBeDisabled();
    });

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

    await waitFor(() => {
      expect(screen.queryByRole("button", { name: "Signing in..." })).not.toBeInTheDocument();
    });
  });
});
