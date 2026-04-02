import type { PropsWithChildren, ReactElement } from "react";

import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";

import { RegisterPage } from "@pages/RegisterPage";
import { AuthProvider } from "@providers/AuthProvider";

const RegisterPageProviders = ({
  children
}: PropsWithChildren): ReactElement => {
  return (
    <MemoryRouter>
      <AuthProvider>{children}</AuthProvider>
    </MemoryRouter>
  );
};

const renderRegisterPage = () => {
  return render(<RegisterPage />, {
    wrapper: RegisterPageProviders
  });
};

describe("RegisterPage", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  test("submits registration and creates an authenticated session", async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn<typeof fetch>();

    fetchMock.mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: "access-register-1",
          refresh_token: "refresh-register-1",
          token_type: "Bearer",
          expires_in: 3600
        }),
        {
          status: 200
        }
      )
    );
    vi.stubGlobal("fetch", fetchMock);

    renderRegisterPage();

    fireEvent.change(screen.getByLabelText(/email/i, { selector: "input" }), {
      target: { value: "demo@example.com" }
    });
    fireEvent.change(screen.getByLabelText(/first name/i), {
      target: { value: "Demo" }
    });
    fireEvent.change(screen.getByLabelText(/last name/i), {
      target: { value: "User" }
    });

    const [passwordInput, confirmationInput] = screen.getAllByLabelText(/password/i, {
      selector: "input"
    });

    fireEvent.change(passwordInput as HTMLElement, {
      target: { value: "StrongPass1!" }
    });
    fireEvent.change(confirmationInput as HTMLElement, {
      target: { value: "StrongPass1!" }
    });

    await user.click(screen.getByRole("button", { name: "Register" }));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith(
        "/auth/register",
        expect.objectContaining({
          body: JSON.stringify({
            email: "demo@example.com",
            first_name: "Demo",
            last_name: "User",
            password: "StrongPass1!"
          }),
          credentials: "include",
          method: "POST"
        })
      );
    });

  });

  test("keeps the register button disabled until the form is valid", async () => {
    renderRegisterPage();

    const submitButton = screen.getByRole("button", { name: "Register" });

    expect(submitButton).toBeDisabled();

    fireEvent.change(screen.getByLabelText(/email/i, { selector: "input" }), {
      target: { value: "demo@example.com" }
    });
    fireEvent.change(screen.getByLabelText(/first name/i), {
      target: { value: "Demo" }
    });
    fireEvent.change(screen.getByLabelText(/last name/i), {
      target: { value: "User" }
    });

    const [passwordInput, confirmationInput] = screen.getAllByLabelText(/password/i, {
      selector: "input"
    });

    fireEvent.change(passwordInput as HTMLElement, {
      target: { value: "StrongPass1!" }
    });
    fireEvent.change(confirmationInput as HTMLElement, {
      target: { value: "StrongPass1!" }
    });

    await waitFor(() => {
      expect(submitButton).toBeEnabled();
    });
  });
});
