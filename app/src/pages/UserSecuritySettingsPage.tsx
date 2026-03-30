import type { ReactElement } from "react";

import { UserSettingsPlaceholderPanel } from "@components/userSettings/UserSettingsPlaceholderPanel";

export const UserSecuritySettingsPage = (): ReactElement => {
  return (
    <UserSettingsPlaceholderPanel
      description="Keep password changes, active-session controls, and future account protection tools in one route-owned section."
      helperText="Current password"
      title="Security"
    />
  );
};
