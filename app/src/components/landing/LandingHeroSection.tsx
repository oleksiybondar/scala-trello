import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { Link as RouterLink } from "react-router-dom";

import { BoardCardView } from "@components/boards/BoardCardView";
import type { TimeVelocityData } from "@components/charts/TimeVelocityChart";
import type { TicketStateCounts } from "@components/boards/tickets-info/types";
import { mapUiTicketStatusToStateKey } from "@helpers/uiTicketStatus";
import type { Board } from "../../domain/board/graphql";

import boardJson from "@assets/home/board.json";
import boardSecondaryJson from "@assets/home/board-secondary.json";

const getHomeBoardTicketCounts = (board: Board): TicketStateCounts => {
  const empty: TicketStateCounts = {
    code_review: 0,
    done: 0,
    in_progress: 0,
    in_testing: 0,
    new: 0
  };

  return board.tickets.reduce((counts, ticket) => {
    const stateKey = mapUiTicketStatusToStateKey(ticket.status);

    if (stateKey === null) {
      return counts;
    }

    return {
      ...counts,
      [stateKey]: counts[stateKey] + 1
    };
  }, empty);
};

const getHomeBoardTimeTrackingData = (board: Board): TimeVelocityData => {
  const estimatedMinutes = board.tickets.reduce((sum, ticket) => {
    return sum + (ticket.estimatedMinutes ?? 0);
  }, 0);
  const actualSeriesMinutes = board.tickets
    .flatMap(ticket => ticket.timeEntries)
    .sort((left, right) => new Date(left.loggedAt).getTime() - new Date(right.loggedAt).getTime())
    .reduce<number[]>((series, entry) => {
      const previous = series[series.length - 1] ?? 0;

      return [...series, previous + Math.max(0, entry.durationMinutes)];
    }, []);

  return {
    actualSeriesMinutes: actualSeriesMinutes.length > 0 ? actualSeriesMinutes : [0],
    estimatedMinutes
  };
};

const homeBoards: Board[] = [boardJson as Board, boardSecondaryJson as Board];

/**
 * Static hero block for the visitor landing page.
 *
 * This component intentionally keeps a larger chunk of presentational markup in
 * one place. The section is mostly fixed introduction content with no
 * meaningful logic, so aggressively splitting it into smaller components would
 * add indirection without improving clarity.
 *
 * Some inner fragments may later be extracted into reusable UI or replaced by
 * real feature-driven widgets, but the section itself should remain a
 * coarse-grained content block.
 */
export const LandingHeroSection = (): ReactElement => {
  return (
    <Paper
      elevation={0}
      sx={{
        border: (theme) => `1px solid ${theme.palette.divider}`,
        overflow: "hidden",
        p: 2.5
      }}
    >
      <Stack
        alignItems={{ md: "flex-start", xs: "stretch" }}
        direction={{ md: "row", xs: "column" }}
        spacing={{ md: 4, xs: 3 }}
      >
        <Stack flex={1} spacing={2}>
          <Stack >
            <Typography variant="h3">Track sprint work without enterprise bloat.</Typography>
            <Typography color="text.secondary" maxWidth={560} variant="body1">
              Boards is a lightweight sprint board application for managing
              tickets, moving them through delivery states, and keeping planned
              work grounded with estimated and logged time.
            </Typography>
            <Typography color="text.secondary" maxWidth={560} variant="body1">
              It is built as a portfolio project, but the model is intentionally
              closer to engineering workflow tools than a generic kanban demo.
            </Typography>
          </Stack>

          <Stack direction={{ sm: "row", xs: "column" }} spacing={2}>
            <Button component={RouterLink} size="large" to="/register" variant="contained">
              Create account
            </Button>
            <Button component={RouterLink} size="large" to="/login" variant="outlined">
              Sign in
            </Button>
          </Stack>
        </Stack>

        <Paper
          elevation={0}
          sx={{
            background:
              "linear-gradient(135deg, rgba(25, 118, 210, 0.14), rgba(25, 118, 210, 0.04))",
            border: (theme) => `1px solid ${theme.palette.divider}`,
            flex: 1,
            p: 1
          }}
        >
          <Stack height="100%" justifyContent="space-between" spacing={3}>
            <Box
              sx={{
                height: { md: 380, xs: 270 },
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
                    md: "translateX(-50%) scale(0.43)",
                    xs: "translateX(-50%) scale(0.30)"
                  }, transformOrigin: "top center",
                  width: 1150,
                }}
              >
                <Stack spacing={2}>
                  {homeBoards.map(board => (
                    <BoardCardView
                      board={board}
                      key={board.boardId}
                      showSettingsButton={false}
                      ticketCounts={getHomeBoardTicketCounts(board)}
                      timeTrackingData={getHomeBoardTimeTrackingData(board)}
                    />
                  ))}
                </Stack>
              </Box>
            </Box>
          </Stack>
        </Paper>
      </Stack>
    </Paper>
  );
};
