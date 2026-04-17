interface TimeTrackingActivityEntry {
  activityId?: string | null;
  activityCode?: string | null;
  activityName: string | null;
  durationMinutes: number;
}

export interface TimeTrackingActivitySlice {
  activityCode: string | null;
  color: string;
  key: string;
  minutes: number;
  name: string;
}

export type ActivityThemeColorToken =
  | "primary.main"
  | "secondary.main"
  | "success.main"
  | "warning.main"
  | "info.main"
  | "error.main";

const ACTIVITY_THEME_COLOR_BY_CODE: Record<string, ActivityThemeColorToken> = {
  code_review: "info.main",
  debugging: "warning.main",
  deployment: "secondary.main",
  design: "secondary.main",
  development: "primary.main",
  devops: "secondary.main",
  documentation: "info.main",
  meeting: "secondary.main",
  qa: "success.main",
  research: "info.main",
  support: "warning.main",
  testing: "success.main"
};

export const resolveActivityThemeColorToken = (
  activityCode: string | null
): ActivityThemeColorToken | null => {
  const normalized = activityCode?.trim().toLowerCase() ?? "";

  return normalized.length === 0 ? null : (ACTIVITY_THEME_COLOR_BY_CODE[normalized] ?? null);
};

export const resolveTimeTrackingActivityName = (
  activityName: string | null,
  activityCode?: string | null,
  activityId?: string | null,
  activityNameById: Record<string, string> = {}
): string => {
  const normalizedName = activityName?.trim() ?? "";
  const normalizedCode = activityCode?.trim() ?? "";
  const normalizedDictionaryName =
    activityId === undefined || activityId === null
      ? ""
      : (activityNameById[activityId]?.trim() ?? "");

  if (normalizedName.length > 0) {
    return normalizedName;
  }

  if (normalizedCode.length > 0) {
    return normalizedCode;
  }

  if (normalizedDictionaryName.length > 0) {
    return normalizedDictionaryName;
  }

  return "Unspecified activity";
};

export const buildTimeTrackingActivitySlices = (
  entries: TimeTrackingActivityEntry[],
  colors: string[],
  activityNameById: Record<string, string> = {}
): TimeTrackingActivitySlice[] => {
  const byActivity = entries.reduce((state, entry) => {
    const name = resolveTimeTrackingActivityName(
      entry.activityName,
      entry.activityCode,
      entry.activityId,
      activityNameById
    );
    const current = state.get(name) ?? {
      activityCode: null as string | null,
      minutes: 0
    };
    const normalizedCode = entry.activityCode?.trim() ?? "";
    const nextActivityCode =
      current.activityCode ?? (normalizedCode.length > 0 ? normalizedCode : null);

    state.set(name, {
      activityCode: nextActivityCode,
      minutes: current.minutes + Math.max(0, entry.durationMinutes)
    });

    return state;
  }, new Map<string, { activityCode: string | null; minutes: number }>());

  return [...byActivity.entries()]
    .sort((left, right) => right[1].minutes - left[1].minutes)
    .map(([name, value], index) => {
      const color = colors[index % colors.length] ?? "#9e9e9e";

      return {
        activityCode: value.activityCode,
        color,
        key: `${name}-${String(index)}`,
        minutes: value.minutes,
        name
      };
    });
};

export const buildTimeTrackingActivityColorMap = (
  slices: TimeTrackingActivitySlice[]
): Record<string, string> => {
  return Object.fromEntries(slices.map(slice => [slice.name, slice.color]));
};
