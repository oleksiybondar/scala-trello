export type UserSettingsSection = "profile" | "security" | "ui-preferences";

export interface UserSettingsNavItem {
  label: string;
  section: UserSettingsSection;
  to: string;
}

export const userSettingsNavItems: UserSettingsNavItem[] = [
  {
    label: "Profile",
    section: "profile",
    to: "/settings/profile"
  },
  {
    label: "Security",
    section: "security",
    to: "/settings/security"
  },
  {
    label: "UI Preferences",
    section: "ui-preferences",
    to: "/settings/ui-preferences"
  }
];
