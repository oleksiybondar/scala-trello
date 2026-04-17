import type { ReactElement } from "react";

import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import {
  TimeVelocityChart,
  type TimeVelocityData
} from "@components/charts/TimeVelocityChart";

interface TimeTrackingInfoProps {
  data: TimeVelocityData;
}

export const TimeTrackingInfo = ({ data }: TimeTrackingInfoProps): ReactElement => {
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
        </Stack>

        <TimeVelocityChart data={data} />
      </Stack>
    </Paper>
  );
};
