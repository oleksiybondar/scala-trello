import type { ReactElement } from "react";

import AccessTimeOutlinedIcon from "@mui/icons-material/AccessTimeOutlined";
import ForumOutlinedIcon from "@mui/icons-material/ForumOutlined";
import TimelineOutlinedIcon from "@mui/icons-material/TimelineOutlined";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { Link as RouterLink } from "react-router-dom";

export const LandingHeroSection = (): ReactElement => {
  return (
    <Paper
      elevation={0}
      sx={{
        border: (theme) => `1px solid ${theme.palette.divider}`,
        overflow: "hidden",
        p: { md: 5, xs: 3 }
      }}
    >
      <Stack
        alignItems="center"
        direction={{ md: "row", xs: "column" }}
        spacing={{ md: 6, xs: 4 }}
      >
        <Stack flex={1} spacing={3}>
          <Stack >
            <Typography variant="h2">Track sprint work without enterprise bloat.</Typography>
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
            minHeight: 320,
            p: 3
          }}
        >
          <Stack height="100%" justifyContent="space-between" spacing={3}>
            <Stack direction="row" spacing={1}>
              <Chip icon={<TimelineOutlinedIcon />} label="Ticket states" variant="outlined" />
              <Chip icon={<AccessTimeOutlinedIcon />} label="Time tracking" variant="outlined" />
              <Chip icon={<ForumOutlinedIcon />} label="Comments" variant="outlined" />
            </Stack>

            <Stack direction={{ sm: "row", xs: "column" }} spacing={2}>
              <Paper sx={{ flex: 1, p: 2 }} variant="outlined">
                <Typography fontWeight={800} variant="subtitle2">
                  Ticket
                </Typography>
                <Stack mt={2} spacing={1}>
                  <Chip label="Estimated: 6h" size="small" sx={{ alignSelf: "flex-start" }} />
                  <Chip label="Logged: 4h 30m" size="small" sx={{ alignSelf: "flex-start" }} />
                  <Chip label="State: In Progress" size="small" sx={{ alignSelf: "flex-start" }} />
                </Stack>
              </Paper>

              <Paper sx={{ flex: 1, p: 2 }} variant="outlined">
                <Typography fontWeight={800} variant="subtitle2">Board</Typography>
                <Stack mt={2} spacing={1.2}>
                  <Typography variant="body2">Backend API sprint</Typography>
                  <Typography color="text.secondary" variant="body2">
                    A board groups the sprint scope, its members, and the
                    tickets moving through implementation, review, and testing.
                  </Typography>
                </Stack>
              </Paper>
            </Stack>

            <Paper sx={{ p: 2.5 }} variant="outlined">
              <Typography variant="body2">
                Each ticket keeps its own estimate, logged work, and discussion,
                so the board shows both delivery state and effort visibility in
                one place.
              </Typography>
            </Paper>
          </Stack>
        </Paper>
      </Stack>
    </Paper>
  );
};
