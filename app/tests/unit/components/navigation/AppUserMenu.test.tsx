import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";

import { AppUserMenu } from "@components/navigation/AppUserMenu";

const navigateMock = vi.fn();
const logoutMock = vi.fn();
const useAuthMock = vi.fn();
const useCurrentUserMock = vi.fn();

vi.mock("@hooks/useAuth", () => ({
  useAuth: () => useAuthMock()
}));

vi.mock("@hooks/useCurrentUser", () => ({
  useCurrentUser: () => useCurrentUserMock()
}));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>(
    "react-router-dom"
  );

  return {
    ...actual,
    useNavigate: () => navigateMock
  };
});

describe("AppUserMenu", () => {
  afterEach(() => {
    logoutMock.mockReset();
    navigateMock.mockReset();
    useAuthMock.mockReset();
    useCurrentUserMock.mockReset();
  });

  test("opens login from the anonymous menu", async () => {
    const user = userEvent.setup();

    useAuthMock.mockReturnValue({
      isAuthenticated: false,
      logout: logoutMock,
      status: "anonymous"
    });
    useCurrentUserMock.mockReturnValue({
      currentUser: null
    });

    render(
      <MemoryRouter>
        <AppUserMenu />
      </MemoryRouter>
    );

    await user.click(screen.getByRole("button", { name: /sign in/i }));
    await user.click(screen.getByRole("menuitem", { name: "Login" }));

    expect(navigateMock).toHaveBeenCalledWith("/login");
    expect(logoutMock).not.toHaveBeenCalled();
  });

  test("shows settings and logout for authenticated user", async () => {
    const user = userEvent.setup();

    useAuthMock.mockReturnValue({
      isAuthenticated: true,
      logout: logoutMock,
      status: "authenticated"
    });
    useCurrentUserMock.mockReturnValue({
      currentUser: {
        avatarUrl: null,
        createdAt: "2026-01-01T00:00:00.000Z",
        email: "alice@example.com",
        firstName: "Alice",
        lastName: "Baker",
        userId: "user-1",
        username: "alice"
      }
    });

    render(
      <MemoryRouter>
        <AppUserMenu />
      </MemoryRouter>
    );

    await user.click(screen.getByRole("button", { name: /alice baker/i }));

    expect(screen.getByRole("menuitem", { name: "Settings" })).toBeInTheDocument();
    expect(screen.getByRole("menuitem", { name: "Logout" })).toBeInTheDocument();

    await user.click(screen.getByRole("menuitem", { name: "Logout" }));

    expect(logoutMock).toHaveBeenCalledTimes(1);
    expect(navigateMock).not.toHaveBeenCalled();
  });
});
