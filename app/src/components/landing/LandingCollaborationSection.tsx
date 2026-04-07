import type { ReactElement } from "react";

import AccessTimeOutlinedIcon from "@mui/icons-material/AccessTimeOutlined";
import CommentOutlinedIcon from "@mui/icons-material/CommentOutlined";
import RuleFolderOutlinedIcon from "@mui/icons-material/RuleFolderOutlined";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

const activities = [
  "development",
  "testing",
  "planning",
  "debugging"
] as const;

export const LandingCollaborationSection = (): ReactElement => {
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
        direction={{ md: "row", xs: "column" }}
        spacing={{ md: 6, xs: 4 }}
      >
        <Stack flex={1} spacing={2}>
          <Typography color="primary" variant="overline">
            Time And Context
          </Typography>
          <Typography variant="h3">Tickets carry effort tracking and discussion with them.</Typography>
          <Typography color="text.secondary" variant="body1">
            Each ticket can hold an estimate, accumulated logged time, and
            comments from board members, so the work item keeps both delivery
            state and execution history in one place.
          </Typography>
          <Typography color="text.secondary" variant="body1">
            Logged time is constrained by a fixed list of activities such as
            development, testing, planning, design, documentation, refinement,
            and debugging.
          </Typography>
        </Stack>

        <Paper
          elevation={0}
          sx={{
            background:
              "linear-gradient(180deg, rgba(46, 125, 50, 0.12), rgba(46, 125, 50, 0.04))",
            border: (theme) => `1px solid ${theme.palette.divider}`,
            flex: 1,
            p: 3,
            width: "100%"
          }}
        >
          <Stack spacing={3}>
            <Stack direction="row" spacing={1}>
              <Chip icon={<AccessTimeOutlinedIcon />} label="Estimate vs logged" variant="outlined" />
              <Chip icon={<CommentOutlinedIcon />} label="Comments" variant="outlined" />
              <Chip icon={<RuleFolderOutlinedIcon />} label="Activity codes" variant="outlined" />
            </Stack>

            <Paper sx={{ p: 2 }} variant="outlined">
              <Typography fontWeight={800} variant="subtitle2">
                Ticket snapshot
              </Typography>
              <Typography color="text.secondary" mt={1} variant="body2">
                Estimated time: 8h. Logged time: 5h 45m. Comments and updates
                stay attached to the ticket instead of being scattered.
              </Typography>
            </Paper>

            <Paper sx={{ p: 2 }} variant="outlined">
              <Typography fontWeight={800} variant="subtitle2">
                Allowed time log activities
              </Typography>
              <Stack direction="row" flexWrap="wrap" gap={1} mt={1.5}>
                {activities.map((activity) => (
                  <Chip key={activity} label={activity} size="small" />
                ))}
              </Stack>
            </Paper>

            <Paper sx={{ p: 2 }} variant="outlined">
              <Typography fontWeight={800} variant="subtitle2">
                Board membership
              </Typography>
              <Typography color="text.secondary" mt={1} variant="body2">
                Members are managed at board level, which keeps permissions and
                ticket discussion scoped to the workspace where the work happens.
              </Typography>
            </Paper>
          </Stack>
        </Paper>
      </Stack>
    </Paper>
  );
};
