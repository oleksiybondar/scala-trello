import type { ReactElement } from "react";

import Grid from "@mui/material/Grid";
import Link from "@mui/material/Link";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { useTheme } from "@mui/material/styles";
import { Link as RouterLink } from "react-router-dom";

import { Person } from "@components/avatar/Person";
import { DonutChart } from "@components/charts/DonutChart";
import { DonutChartLegendItem } from "@components/charts/DonutChartLegendItem";
import {
  TimeVelocityChart,
  type TimeVelocityStats
} from "@components/charts/TimeVelocityChart";
import { formatMinutesToTimeTrackingDuration } from "@helpers/timeTrackingConversions";
import type { Ticket } from "../../../domain/ticket/graphql";

interface TicketDetailsLayoutProps {
  ticket: Ticket;
}

interface ActivitySlice {
  color: string;
  key: string;
  minutes: number;
  name: string;
}

const getTimeVelocityStats = (ticket: Ticket): TimeVelocityStats => {
  const estimatedMinutes = ticket.estimatedMinutes ?? 0;
  const loggedMinutes = ticket.timeEntries.reduce((sum, entry) => {
    return sum + entry.durationMinutes;
  }, 0);

  return {
    estimatedTime: formatMinutesToTimeTrackingDuration(estimatedMinutes),
    loggedTime: formatMinutesToTimeTrackingDuration(loggedMinutes),
    overdueTime: formatMinutesToTimeTrackingDuration(loggedMinutes - estimatedMinutes)
  };
};

const getActivitySlices = (
  ticket: Ticket,
  colors: string[]
): ActivitySlice[] => {
  const byActivity = ticket.timeEntries.reduce((state, entry) => {
    const label = entry.activityName?.trim() ?? "";
    const key = label.length > 0 ? label : "Unspecified activity";
    const currentMinutes = state.get(key) ?? 0;

    state.set(key, currentMinutes + entry.durationMinutes);

    return state;
  }, new Map<string, number>());

  return [...byActivity.entries()]
    .sort((left, right) => right[1] - left[1])
    .map(([name, minutes], index) => {
      const color = colors[index % colors.length] ?? "#9e9e9e";

      return {
        color,
        key: `${name}-${String(index)}`,
        minutes,
        name
      };
    });
};

export const TicketDetailsLayout = ({ ticket }: TicketDetailsLayoutProps): ReactElement => {
  const theme = useTheme();
  const timeVelocityStats = getTimeVelocityStats(ticket);
  const activityColors = [
    theme.palette.primary.main,
    theme.palette.success.main,
    theme.palette.warning.main,
    theme.palette.info.main,
    theme.palette.error.main
  ];
  const activitySlices = getActivitySlices(ticket, activityColors);
  const totalActivityMinutes = activitySlices.reduce((sum, slice) => sum + slice.minutes, 0);
  const isOverdue = timeVelocityStats.overdueTime !== "0h:00m";

  return (
    <Grid container spacing={3}>
      <Grid size={{ lg: 8, xs: 12 }}>
        <Paper sx={{ p: 2 }} variant="outlined">
          <Stack spacing={2}>
            <Stack spacing={0.75}>
              <Typography variant="h2">{ticket.name}</Typography>
              <Link
                color="text.secondary"
                component={RouterLink}
                to={`/boards/${ticket.boardId}`}
                underline="hover"
                variant="body2"
              >
                {ticket.board?.name ?? "Open board"}
              </Link>
            </Stack>

            <Grid container spacing={2}>
              <Grid size={{ md: 6, xs: 12 }}>
                <Person fallbackLabel="Unknown creator" person={ticket.createdBy} />
              </Grid>
              <Grid size={{ md: 6, xs: 12 }}>
                <Person fallbackLabel="Unassigned" person={ticket.assignedTo} />
              </Grid>
            </Grid>

            <Stack spacing={1}>
              <Typography variant="subtitle2">Description</Typography>
              <Typography color="text.secondary" variant="body2">
                {ticket.description?.trim().length ? ticket.description : "No description provided."}
              </Typography>
            </Stack>

            <Stack spacing={1}>
              <Typography variant="subtitle2">Acceptance criteria</Typography>
              <Typography color="text.secondary" variant="body2">
                {ticket.acceptanceCriteria?.trim().length
                  ? ticket.acceptanceCriteria
                  : "No acceptance criteria provided."}
              </Typography>
            </Stack>
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ lg: 4, xs: 12 }}>
        <Stack spacing={2}>
          <Paper sx={{ p: 1.5 }} variant="outlined">
            <Stack spacing={1.25}>
              <Stack alignItems="center" direction="row" justifyContent="space-between" spacing={1}>
                <Typography variant="subtitle2">Time velocity</Typography>
                {isOverdue ? (
                  <Typography color="warning.main" sx={{ fontWeight: 700 }} variant="caption">
                    Overdue: {timeVelocityStats.overdueTime}
                  </Typography>
                ) : null}
              </Stack>
              <TimeVelocityChart stats={timeVelocityStats} />
            </Stack>
          </Paper>

          <Paper sx={{ p: 1.5 }} variant="outlined">
            <Stack spacing={1.5}>
              <Typography variant="subtitle2">Activity</Typography>
              <Stack
                alignItems="center"
                direction={{ md: "row", xs: "column" }}
                justifyContent="center"
                spacing={2}
              >
                <Stack alignItems="center" justifyContent="center" spacing={1} sx={{ flex: "0 0 160px" }}>
                  <DonutChart
                    centerLabel="Logged"
                    centerValue={formatMinutesToTimeTrackingDuration(totalActivityMinutes)}
                    segments={activitySlices.map(slice => ({
                      color: slice.color,
                      value: slice.minutes
                    }))}
                  />
                </Stack>
                <Stack spacing={1} sx={{ flex: 1, minWidth: 0 }}>
                  {activitySlices.length === 0 ? (
                    <Typography color="text.secondary" variant="body2">
                      No activity tracked yet.
                    </Typography>
                  ) : (
                    activitySlices.map(slice => (
                      <DonutChartLegendItem
                        color={slice.color}
                        key={slice.key}
                        value={`${slice.name}: ${formatMinutesToTimeTrackingDuration(slice.minutes)}`}
                      />
                    ))
                  )}
                </Stack>
              </Stack>
            </Stack>
          </Paper>
        </Stack>
      </Grid>
    </Grid>
  );
};
