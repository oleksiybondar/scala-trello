import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";

import { AppProviders } from "@providers/AppProviders";
import { UserProfileSettingsPage } from "@pages/UserProfileSettingsPage";
import { UserSecuritySettingsPage } from "@pages/UserSecuritySettingsPage";
import { UserSettingsPage } from "@pages/UserSettingsPage";
import { UserUiPreferencesSettingsPage } from "@pages/UserUiPreferencesSettingsPage";

const renderSettingsRoute = (initialEntry: string) => {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <AppProviders>
        <Routes>
          <Route path="/settings" element={<UserSettingsPage />}>
            <Route path="profile" element={<UserProfileSettingsPage />} />
            <Route path="security" element={<UserSecuritySettingsPage />} />
            <Route
              path="ui-preferences"
              element={<UserUiPreferencesSettingsPage />}
            />
          </Route>
        </Routes>
      </AppProviders>
    </MemoryRouter>
  );
};

describe("UserSettingsPage", () => {
  test("renders the profile route with the matching sidebar item selected", () => {
    renderSettingsRoute("/settings/profile");

    expect(
      screen.getByRole("heading", { name: "User settings" })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("link", { name: "Profile" })
    ).toHaveAttribute("aria-current", "page");
    expect(
      screen.getByRole("heading", { name: "Profile details" })
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Edit" })).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "First name" })).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "Last name" })).toBeInTheDocument();
  });

  test("switches sections through route navigation from the sidebar", async () => {
    const user = userEvent.setup();

    renderSettingsRoute("/settings/profile");

    await user.click(
      screen.getByRole("link", {
        name: "UI Preferences"
      })
    );

    expect(
      screen.getByRole("link", {
        name: "UI Preferences"
      })
    ).toHaveAttribute("aria-current", "page");
    expect(
      screen.getByRole("heading", { name: "Theme preferences" })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("combobox", { name: "Theme source" })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("combobox", { name: "Theme mode" })
    ).toHaveAttribute("aria-disabled", "true");
    expect(
      screen.getByRole("combobox", { name: "Theme template" })
    ).toHaveAttribute("aria-disabled", "true");
  });
});
