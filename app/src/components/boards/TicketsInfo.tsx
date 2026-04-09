import type { ReactElement } from "react";

import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import { useTheme } from "@mui/material/styles";

import { TicketPieChart } from "@components/boards/tickets-info/TicketPieChart";
import { TicketPieChartLegendItem } from "@components/boards/tickets-info/TicketPieChartLegendItem";
import type {
  TicketStateCounts,
  TicketStateKey
} from "@components/boards/tickets-info/types";

interface TicketsInfoProps {
  ticketCounts: TicketStateCounts;
}

interface TicketStateDefinition {
  key: TicketStateKey;
  paletteColor: string;
  title: string;
}

const ticketStates: TicketStateDefinition[] = [
  {
    key: "new",
    paletteColor: "warning.main",
    title: "New"
  },
  {
    key: "in_progress",
    paletteColor: "info.main",
    title: "In progress"
  },
  {
    key: "code_review",
    paletteColor: "secondary.main",
    title: "Code review"
  },
  {
    key: "in_testing",
    paletteColor: "primary.main",
    title: "In Testing"
  },
  {
    key: "done",
    paletteColor: "success.main",
    title: "Done"
  }
];

const resolvePaletteColor = (
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

export const TicketsInfo = ({ ticketCounts }: TicketsInfoProps): ReactElement => {
  const theme = useTheme();
  const segments = ticketStates.map((state, index) => {
    const count = ticketCounts[state.key];

    return {
      color: resolvePaletteColor(
        state.paletteColor,
        theme.palette as unknown as Record<string, unknown>,
        theme.palette.grey[400]
      ),
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
          <TicketPieChart
            segments={segments.map(segment => ({
              color: segment.color,
              value: segment.count
            }))}
            total={totalTickets}
          />
        </Stack>

        <Stack
          justifyContent="center"
          spacing={1}
          sx={{ flex: 1, maxWidth: 280, minWidth: 0 }}
        >
          {segments.map(segment => (
            <TicketPieChartLegendItem
              color={segment.color}
              count={segment.count}
              key={segment.key}
              title={segment.title}
            />
          ))}
        </Stack>
      </Stack>
    </Paper>
  );
};
