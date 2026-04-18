import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { alpha, useTheme } from "@mui/material/styles";

import { BoardTicketCardView } from "@components/boards/board-page/BoardTicketCardView";
import type { Ticket } from "../../domain/ticket/graphql";

import ticketJson from "@assets/home/ticket.json";

const columns = [
  "New",
  "In Progress",
  "Code Review",
  "In Testing",
  "Done"
] as const;

export const LandingBoardsSection = (): ReactElement => {
  const theme = useTheme();
  const homeTicketExample = ticketJson as Ticket;
  const demoInProgressTicket = {
    ...homeTicketExample,
    status: "in progress" as const,
    ticketId: "landing-ticket-in-progress"
  };
  const demoReviewTicket = {
    ...homeTicketExample,
    name: "Validate resolver complexity",
    status: "code review" as const,
    ticketId: "landing-ticket-code-review",
    trackedMinutes: 120
  };

  return (
    <Paper
      elevation={0}
      sx={{
        border: (themeValue) => `1px solid ${themeValue.palette.divider}`,
        p: { md: 4, xs: 2.5 }
      }}
    >
      <Stack
        alignItems={{ md: "flex-start", xs: "stretch" }}
        direction={{ md: "row", xs: "column-reverse" }}
        spacing={{ md: 4, xs: 3 }}
      >
        <Paper sx={{ flex: 1, p: 1.25, width: "100%" }} variant="outlined">
          <Box
            aria-label="Landing boards board-main-area preview"
            sx={{
              height: 200,
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
                  xs: "translateX(-50%) scale(0.40)"
                },
                transformOrigin: "top center",
                width: 1150
              }}
            >
              <Box
                sx={{
                  display: "grid",
                  gap: 1,
                  gridTemplateColumns: "repeat(5, minmax(0, 1fr))"
                }}
              >
                {columns.map(columnTitle => (
                  <Paper
                    key={columnTitle}
                    sx={{
                      bgcolor: "background.default",
                      display: "flex",
                      flexDirection: "column",
                      minHeight: 470,
                      overflow: "hidden"
                    }}
                    variant="outlined"
                  >
                    <Box
                      sx={{
                        backdropFilter: "blur(10px)",
                        bgcolor:
                          columnTitle === "In Progress"
                            ? alpha(theme.palette.warning.main, 0.1)
                            : columnTitle === "Code Review"
                              ? alpha(theme.palette.info.main, 0.1)
                              : theme.palette.background.paper,
                        borderBottom: (themeValue) => "1px solid " + themeValue.palette.divider,
                        px: 2,
                        py: 1.5
                      }}
                    >
                      <Typography variant="subtitle1">{columnTitle}</Typography>
                    </Box>
                    <Stack spacing={1} sx={{ flex: 1, minHeight: 240, px: 2, py: 2 }}>
                      {columnTitle === "In Progress" ? (
                        <BoardTicketCardView disableActions ticket={demoInProgressTicket} />
                      ) : null}
                      {columnTitle === "Code Review" ? (
                        <BoardTicketCardView disableActions ticket={demoReviewTicket} />
                      ) : null}
                      {columnTitle !== "In Progress" && columnTitle !== "Code Review" ? (
                        <Typography color="text.secondary" variant="caption">
                          No tickets yet.
                        </Typography>
                      ) : null}
                    </Stack>
                  </Paper>
                ))}
              </Box>
            </Box>
          </Box>
        </Paper>

        <Stack flex={1} spacing={2}>
          <Typography color="primary" variant="overline">
            Workflow States
          </Typography>
          <Typography variant="h5">Tickets move through a delivery-focused lifecycle.</Typography>
          <Typography color="text.secondary" variant="body1">
            Each board acts as a sprint workspace, but the work inside it is
            shaped by explicit engineering states rather than generic columns.
          </Typography>
          <Typography color="text.secondary" variant="body1">
            The default flow is `New`, `In Progress`, `Code Review`,
            `In Testing`, and `Done`, which makes the board useful for real
            development work instead of just task listing.
          </Typography>
        </Stack>
      </Stack>
    </Paper>
  );
};
