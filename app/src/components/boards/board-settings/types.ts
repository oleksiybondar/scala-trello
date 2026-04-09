export type BoardSettingsSection = "general" | "members" | "ownership";

export interface BoardSettingsNavItem {
  label: string;
  section: BoardSettingsSection;
}

export const boardSettingsNavItems: BoardSettingsNavItem[] = [
  {
    label: "General",
    section: "general"
  },
  {
    label: "Members",
    section: "members"
  },
  {
    label: "Ownership",
    section: "ownership"
  }
];
