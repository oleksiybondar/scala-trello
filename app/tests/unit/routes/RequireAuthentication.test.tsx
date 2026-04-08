import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";

import { AuthContext } from "@contexts/auth-context";
import type { AuthContextValue } from "@contexts/auth-context";
import { RequireAuthentication } from "@routes/RequireAuthentication";

const renderProtectedRoute = (authValue: Partial<AuthContextValue>) => {
  return render(
    <AuthContext.Provider
      value={{
        accessToken: null,
        isAuthenticated: false,
        login: vi.fn(),
        logout: vi.fn(),
        refreshSession: vi.fn(),
        register: vi.fn(),
        session: null,
        status: "anonymous",
        ...authValue
      }}
    >
      <MemoryRouter initialEntries={["/boards"]}>
        <Routes>
          <Route path="/login" element={<div>Login page</div>} />
          <Route
            path="/boards"
            element={
              <RequireAuthentication>
                <div>Protected boards</div>
              </RequireAuthentication>
            }
          />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>
  );
};

describe("RequireAuthentication", () => {
  test("redirects anonymous users to login", () => {
    renderProtectedRoute({});

    expect(screen.getByText("Login page")).toBeInTheDocument();
    expect(screen.queryByText("Protected boards")).not.toBeInTheDocument();
  });

  test("renders protected content for authenticated users", () => {
    renderProtectedRoute({
      accessToken: "access-1",
      isAuthenticated: true,
      session: {
        accessToken: "access-1",
        expiresAt: Date.now() + 60_000,
        expiresIn: 3600,
        refreshToken: "refresh-1",
        tokenType: "Bearer"
      },
      status: "authenticated"
    });

    expect(screen.getByText("Protected boards")).toBeInTheDocument();
    expect(screen.queryByText("Login page")).not.toBeInTheDocument();
  });
});
