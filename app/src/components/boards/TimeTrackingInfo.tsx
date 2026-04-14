import type { ReactElement } from "react";

import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import {
  TimeVelocityChart,
  type TimeVelocityStats
} from "@components/charts/TimeVelocityChart";

interface TimeTrackingInfoProps {
  stats: TimeVelocityStats;
}

export const TimeTrackingInfo = ({ stats }: TimeTrackingInfoProps): ReactElement => {
  const isOverdue = stats.overdueTime !== "0h:00m";

  return (
    <Paper
      sx={{
        borderRadius: 0.5,
        flex: "1 1 320px",
        p: 0.5
      }}
      variant="outlined"
    >
      <Stack spacing={1.25}>
        <Stack
          alignItems="center"
          direction="row"
          justifyContent="space-between"
          spacing={1}
        >
          <Typography variant="subtitle2">Time tracking</Typography>
          {isOverdue ? (
            <Typography
              color="warning.main"
              sx={{ fontWeight: 700 }}
              variant="caption"
            >
              Overdue: {stats.overdueTime}
            </Typography>
          ) : null}
        </Stack>

        <TimeVelocityChart stats={stats} />
      </Stack>
    </Paper>
  );
};
