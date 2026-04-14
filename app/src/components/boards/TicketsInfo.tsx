import type { ReactElement } from "react";

import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import { useTheme } from "@mui/material/styles";

import { DonutChart } from "@components/charts/DonutChart";
import { DonutChartLegendItem } from "@components/charts/DonutChartLegendItem";
import type { TicketStateCounts } from "@components/boards/tickets-info/types";
import {
  boardTicketStates,
  resolveBoardTicketStateColor
} from "@helpers/boardTicketState";

interface TicketsInfoProps {
  ticketCounts: TicketStateCounts;
}

export const TicketsInfo = ({ ticketCounts }: TicketsInfoProps): ReactElement => {
  const theme = useTheme();
  const segments = boardTicketStates.map((state, index) => {
    const count = ticketCounts[state.key];

    return {
      color: resolveBoardTicketStateColor(theme, state.paletteColor),
      count,
      key: state.key + "-" + String(index),
      title: state.title
    };
  });
  const totalTickets = segments.reduce((sum, segment) => sum + segment.count, 0);

  return (
    <Paper
      sx={{
        borderRadius: 0.5,
        flex: "1 1 420px",
        p: 0.5
      }}
      variant="outlined"
    >
      <Stack
        alignItems="center"
        direction={{ md: "row", xs: "column" }}
        justifyContent="center"
        spacing={2}
        sx={{ height: "100%" }}
      >
        <Stack
          alignItems="center"
          justifyContent="center"
          spacing={1}
          sx={{ flex: "0 0 160px", minWidth: 140 }}
        >
          <DonutChart
            centerLabel="Total"
            centerValue={totalTickets}
            segments={segments.map(segment => ({
              color: segment.color,
              value: segment.count
            }))}
          />
        </Stack>

        <Stack
          justifyContent="center"
          spacing={1}
          sx={{ flex: 1, maxWidth: 280, minWidth: 0 }}
        >
          {segments.map(segment => (
            <DonutChartLegendItem
              color={segment.color}
              key={segment.key}
              value={`${segment.title}: ${String(segment.count)}`}
            />
          ))}
        </Stack>
      </Stack>
    </Paper>
  );
};
