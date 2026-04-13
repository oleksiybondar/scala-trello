import type { Theme } from "@mui/material/styles";

import type { UiTicketStateKey, UiTicketStatus } from "@helpers/uiTicketStatus";

export interface BoardTicketStateDefinition {
  key: UiTicketStateKey;
  paletteColor: string;
  status: UiTicketStatus;
  title: string;
}

export const boardTicketStates: BoardTicketStateDefinition[] = [
  {
    key: "new",
    paletteColor: "warning.main",
    status: "new",
    title: "New"
  },
  {
    key: "in_progress",
    paletteColor: "info.main",
    status: "in progress",
    title: "In progress"
  },
  {
    key: "code_review",
    paletteColor: "secondary.main",
    status: "code review",
    title: "Code review"
  },
  {
    key: "in_testing",
    paletteColor: "primary.main",
    status: "in testing",
    title: "In Testing"
  },
  {
    key: "done",
    paletteColor: "success.main",
    status: "done",
    title: "Done"
  }
];

export const resolvePaletteColor = (
  path: string,
  palette: Record<string, unknown>,
  fallback: string
): string => {
  const resolved = path.split(".").reduce<unknown>((value, key) => {
    if (value !== null && typeof value === "object" && key in value) {
      return (value as Record<string, unknown>)[key];
    }

    return undefined;
  }, palette);

  return typeof resolved === "string" ? resolved : fallback;
};

export const resolveBoardTicketStateColor = (
  theme: Theme,
  paletteColor: string
): string => {
  return resolvePaletteColor(
    paletteColor,
    theme.palette as unknown as Record<string, unknown>,
    theme.palette.grey[400]
  );
};
