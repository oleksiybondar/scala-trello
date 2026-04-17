import { useTheme } from "@mui/material/styles";
import type { Theme } from "@mui/material/styles";

import {
  type ActivityThemeColorToken,
  buildTimeTrackingActivityColorMap,
  buildTimeTrackingActivitySlices,
  type TimeTrackingActivitySlice,
  resolveActivityThemeColorToken
} from "@helpers/timeTrackingActivities";
import type { TicketTimeTrackingEntry } from "../domain/ticket/graphql";

interface UseTimeTrackingActivityChartResult {
  activityColorByName: Record<string, string>;
  activitySlices: TimeTrackingActivitySlice[];
  totalActivityMinutes: number;
}

const resolveThemeColorByToken = (
  theme: Theme,
  token: ActivityThemeColorToken | null
): string | null => {
  switch (token) {
    case "error.main":
      return theme.palette.error.main;
    case "info.main":
      return theme.palette.info.main;
    case "primary.main":
      return theme.palette.primary.main;
    case "secondary.main":
      return theme.palette.secondary.main;
    case "success.main":
      return theme.palette.success.main;
    case "warning.main":
      return theme.palette.warning.main;
    default:
      return null;
  }
};

export const useTimeTrackingActivityChart = (
  entries: TicketTimeTrackingEntry[],
  activityNameById: Record<string, string> = {}
): UseTimeTrackingActivityChartResult => {
  const theme = useTheme();
  const neutralFallbackColor = theme.palette.grey[500];
  const activitySlicesWithFallbackColors = buildTimeTrackingActivitySlices(
    entries,
    [neutralFallbackColor],
    activityNameById
  );
  const activitySlices = activitySlicesWithFallbackColors.map(slice => {
    const mappedColor = resolveThemeColorByToken(
      theme,
      resolveActivityThemeColorToken(slice.activityCode)
    );

    return {
      ...slice,
      color: mappedColor ?? neutralFallbackColor
    };
  });

  return {
    activityColorByName: buildTimeTrackingActivityColorMap(activitySlices),
    activitySlices,
    totalActivityMinutes: activitySlices.reduce((sum, slice) => sum + slice.minutes, 0)
  };
};
