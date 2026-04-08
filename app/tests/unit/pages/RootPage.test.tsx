import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";

import { AuthContext } from "@contexts/auth-context";
import type { AuthContextValue } from "@contexts/auth-context";
import { RootPage } from "@pages/RootPage";

const renderRootPage = (authValue: Partial<AuthContextValue>) => {
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
      <MemoryRouter initialEntries={["/"]}>
        <Routes>
          <Route path="/" element={<RootPage />} />
          <Route path="/boards" element={<div>Boards page</div>} />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>
  );
};

describe("RootPage", () => {
  test("renders the landing page for anonymous users", () => {
    renderRootPage({});

    expect(
      screen.getByRole("heading", {
        name: "Track sprint work without enterprise bloat."
      })
    ).toBeInTheDocument();
  });

  test("redirects authenticated users to boards", () => {
    renderRootPage({
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

    expect(screen.getByText("Boards page")).toBeInTheDocument();
  });
});
