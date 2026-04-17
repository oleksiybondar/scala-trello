import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import {
  TimeVelocityChart,
  type TimeVelocityData
} from "@components/charts/TimeVelocityChart";
import { TimeTrackingActivityPieChart } from "@components/time-tracking/TimeTrackingActivityPieChart";
import { TicketActivityEntryList } from "@components/tickets/ticket-page/TicketActivityEntryList";
import { formatMinutesToTimeTrackingDuration } from "@helpers/timeTrackingConversions";
import { useTimeTrackingActivityChart } from "@hooks/useTimeTrackingActivityChart";
import type { Ticket } from "../../domain/ticket/graphql";

import ticketJson from "@assets/home/ticket.json";

const getTimeVelocityData = (ticket: Ticket): TimeVelocityData => {
  const estimatedMinutes = ticket.estimatedMinutes ?? 0;
  const sortedEntries = [...ticket.timeEntries].sort((left, right) => {
    return new Date(left.loggedAt).getTime() - new Date(right.loggedAt).getTime();
  });
  const actualSeriesMinutes: number[] = [];
  let runningMinutes = 0;

  sortedEntries.forEach(entry => {
    runningMinutes += Math.max(0, entry.durationMinutes);
    actualSeriesMinutes.push(runningMinutes);
  });

  return {
    actualSeriesMinutes: actualSeriesMinutes.length > 0 ? actualSeriesMinutes : [0],
    estimatedMinutes
  };
};

export const LandingTimeTracking = (): ReactElement => {
  const homeTicketExample = ticketJson as Ticket;
  const timeVelocityData = getTimeVelocityData(homeTicketExample);
  const { activityColorByName, activitySlices, totalActivityMinutes } = useTimeTrackingActivityChart(
    homeTicketExample.timeEntries
  );

  return (
    <Paper
      elevation={0}
      sx={{
        border: themeValue => `1px solid ${themeValue.palette.divider}`,
        p: { md: 4, xs: 2.5 }
      }}
    >
      <Stack
        alignItems={{ md: "flex-start", xs: "stretch" }}
        direction={{ md: "row", xs: "column" }}
        spacing={{ md: 4, xs: 3 }}
      >
        <Stack flex={1} spacing={2}>
          <Typography color="primary" variant="overline">
            Time And Context
          </Typography>
          <Typography variant="h5">Tickets carry effort tracking and discussion with them.</Typography>
          <Typography color="text.secondary" variant="body1">
            Teams can compare estimate versus logged time, then inspect activity
            distribution and the latest logged entries in the same view.
          </Typography>
          <Typography color="text.secondary" variant="body1">
            The graph and activity list reflect real ticket data components, so
            home preview and board experience stay visually consistent.
          </Typography>
        </Stack>

        <Paper sx={{ flex: 1, p: 1.25, width: "100%" }} variant="outlined">
          <Box
            aria-label="Landing time tracking preview"
            sx={{
              height: 240,
              overflow: "hidden",
              position: "relative",
              width: "100%"
            }}
          >
            <Box
              sx={{
                left: "50%",
                position: "absolute",
                top: 0,
                transform: {
                  md: "translateX(-50%) scale(0.55)",
                  xs: "translateX(-50%) scale(0.55)"
                },
                transformOrigin: "top center",
                width: 920
              }}
            >
              <Box
                sx={{
                  display: "grid",
                  gap: 1,
                  gridTemplateColumns: "1fr 1fr"
                }}
              >
                <Paper
                  sx={{
                    bgcolor: "background.default",
                    minHeight: 380,
                    p: 2
                  }}
                  variant="outlined"
                >
                  <Stack spacing={2}>
                    <Typography variant="subtitle1">Time velocity</Typography>
                    <TimeVelocityChart chartHeight={108} data={timeVelocityData} />

                    <Typography variant="subtitle1">Activity</Typography>
                    <TimeTrackingActivityPieChart
                      activitySlices={activitySlices}
                      direction="row"
                      legendLabelResolver={slice =>
                        `${slice.name} • ${formatMinutesToTimeTrackingDuration(slice.minutes)}`
                      }
                      totalActivityMinutes={totalActivityMinutes}
                    />
                  </Stack>
                </Paper>

                <Paper
                  sx={{
                    bgcolor: "background.default",
                    minHeight: 380,
                    p: 2
                  }}
                  variant="outlined"
                >
                  <Stack spacing={2}>
                    <Typography variant="subtitle1">Activity log</Typography>
                    <TicketActivityEntryList
                      activityColorByName={activityColorByName}
                      entries={homeTicketExample.timeEntries}
                    />
                  </Stack>
                </Paper>
              </Box>
            </Box>
          </Box>
        </Paper>
      </Stack>
    </Paper>
  );
};
