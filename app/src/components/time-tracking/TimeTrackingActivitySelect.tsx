import type { ReactElement } from "react";

import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";

import type { DictionaryTimeTrackingActivity } from "../../domain/dictionaries/graphql";

interface TimeTrackingActivitySelectProps {
  activities: DictionaryTimeTrackingActivity[];
  disabled?: boolean | undefined;
  onChange: (activityId: string) => void;
  value: string;
}

export const TimeTrackingActivitySelect = ({
  activities,
  disabled = false,
  onChange,
  value
}: TimeTrackingActivitySelectProps): ReactElement => {
  return (
    <FormControl fullWidth size="small">
      <InputLabel id="time-tracking-activity-label">Activity</InputLabel>
      <Select
        disabled={disabled}
        label="Activity"
        labelId="time-tracking-activity-label"
        onChange={event => {
          onChange(event.target.value);
        }}
        value={value}
      >
        {activities.map(activity => {
          return (
            <MenuItem key={activity.activityId} value={activity.activityId}>
              {activity.name}
            </MenuItem>
          );
        })}
      </Select>
    </FormControl>
  );
};
