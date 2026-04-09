export type BoardSettingsSection = "general" | "members" | "ownership";

export interface BoardSettingsNavItem {
  label: string;
  isVisible: (permissions: {
    canCreate: boolean;
    canDelete: boolean;
    canModify: boolean;
    canRead: boolean;
    canReassign: boolean;
  }) => boolean;
  section: BoardSettingsSection;
}

export const boardSettingsNavItems: BoardSettingsNavItem[] = [
  {
    label: "General",
    isVisible: permissions => permissions.canModify,
    section: "general"
  },
  {
    label: "Members",
    isVisible: permissions =>
      permissions.canRead ||
      permissions.canCreate ||
      permissions.canModify ||
      permissions.canDelete,
    section: "members"
  },
  {
    label: "Ownership",
    isVisible: permissions =>
      permissions.canModify || permissions.canDelete || permissions.canReassign,
    section: "ownership"
  }
];
