import type { ReactElement } from "react";

import Chip from "@mui/material/Chip";
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";
import { alpha, useTheme } from "@mui/material/styles";
import type { Theme } from "@mui/material/styles";

import { resolveActivityThemeColorToken } from "@helpers/timeTrackingActivities";
import type { DictionaryTimeTrackingActivity } from "../../domain/dictionaries/graphql";

interface TimeTrackingActivitySelectProps {
  activities: DictionaryTimeTrackingActivity[];
  disabled?: boolean | undefined;
  emptyLabel?: string | undefined;
  label?: string | undefined;
  labelId?: string | undefined;
  multiple?: boolean | undefined;
  onChange: (activityId: string | string[]) => void;
  value: string | string[];
}

const resolveThemeColorByCode = (theme: Theme, code: string): string => {
  const token = resolveActivityThemeColorToken(code);

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
      return theme.palette.grey[500];
  }
};

export const TimeTrackingActivitySelect = ({
  activities,
  disabled = false,
  emptyLabel = "Any",
  label = "Activity",
  labelId = "time-tracking-activity-label",
  multiple = false,
  onChange,
  value
}: TimeTrackingActivitySelectProps): ReactElement => {
  const theme = useTheme();
  const activityById = new Map(activities.map(activity => [activity.activityId, activity]));

  return (
    <FormControl fullWidth size="small">
      <InputLabel id={labelId}>{label}</InputLabel>
      <Select
        disabled={disabled}
        label={label}
        labelId={labelId}
        multiple={multiple}
        onChange={event => {
          const rawValue = event.target.value;

          if (multiple) {
            onChange(
              typeof rawValue === "string" ? rawValue.split(",").filter(Boolean) : rawValue
            );
            return;
          }

          onChange(typeof rawValue === "string" ? rawValue : (rawValue[0] ?? ""));
        }}
        renderValue={selected => {
          if (multiple) {
            const selectedIds =
              typeof selected === "string" ? selected.split(",").filter(Boolean) : selected;

            if (selectedIds.length === 0) {
              return emptyLabel;
            }

            if (selectedIds.length === 1) {
              return activityById.get(selectedIds[0] ?? "")?.name ?? emptyLabel;
            }

            return `${String(selectedIds.length)} selected`;
          }

          if (typeof selected !== "string" || selected.length === 0) {
            return emptyLabel;
          }

          return activityById.get(selected)?.name ?? emptyLabel;
        }}
        value={value}
      >
        {activities.map(activity => {
          const color = resolveThemeColorByCode(theme, activity.code);

          return (
            <MenuItem key={activity.activityId} value={activity.activityId}>
              <Chip
                label={activity.name}
                size="small"
                sx={{
                  backgroundColor: alpha(color, 0.14),
                  border: `1px solid ${alpha(color, 0.35)}`,
                  color
                }}
                variant="outlined"
              />
            </MenuItem>
          );
        })}
      </Select>
    </FormControl>
  );
};
