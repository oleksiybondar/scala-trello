import type { ReactElement } from "react";

import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

const workflowStates = [
  {
    description: "Ticket has been created and is awaiting work.",
    title: "New"
  },
  {
    description: "Work on the ticket is currently in progress.",
    title: "In progress"
  },
  {
    description: "Implementation is ready for review.",
    title: "Code review"
  },
  {
    description: "Changes are being verified.",
    title: "In Testing"
  },
  {
    description: "Work is completed.",
    title: "Done"
  }
] as const;

export const LandingBoardsSection = (): ReactElement => {
  return (
    <Paper
      elevation={0}
      sx={{
        border: (theme) => `1px solid ${theme.palette.divider}`,
        p: { md: 5, xs: 3 }
      }}
    >
      <Stack
        alignItems="center"
        direction={{ md: "row", xs: "column-reverse" }}
        spacing={{ md: 6, xs: 4 }}
      >
        <Stack flex={1} spacing={1.5} width="100%">
          {workflowStates.map(({ description, title }) => (
            <Paper key={title} sx={{ p: 1.2 }} variant="outlined">
              <Typography fontWeight={800} gutterBottom variant="subtitle1">
                {title}
              </Typography>
              <Typography color="text.secondary" variant="body2">
                {description}
              </Typography>
            </Paper>
          ))}
        </Stack>

        <Stack flex={1} spacing={2}>
          <Typography color="primary" variant="overline">
            Workflow States
          </Typography>
          <Typography variant="h3">Tickets move through a delivery-focused lifecycle.</Typography>
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
