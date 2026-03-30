import type { ReactElement } from "react";

import { UserSettingsPlaceholderPanel } from "@components/userSettings/UserSettingsPlaceholderPanel";

export const UserUiPreferencesSettingsPage = (): ReactElement => {
  return (
    <UserSettingsPlaceholderPanel
      description="Reserve a dedicated route for theme, density, and future per-user interface preferences."
      helperText="Preferred language"
      title="UI Preferences"
    />
  );
};
