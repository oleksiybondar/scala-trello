import type { ReactElement } from "react";

import Button from "@mui/material/Button";
import Container from "@mui/material/Container";
import Link from "@mui/material/Link";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { Link as RouterLink } from "react-router-dom";

import { ThemeWidget } from "@components/theme/ThemeWidget";
import { useThemeManager } from "@hooks/useThemeManager";

export const HomePage = (): ReactElement => {
  const { mode, resolvedMode, resolvedTemplateName, source, templateName } =
    useThemeManager();

  return (
    <Container>
      <Stack minHeight="100vh" justifyContent="center" py={6}>
        <Stack spacing={3}>
          <Paper>
          <Stack spacing={3} p={4}>
            <Stack spacing={1}>
              <Typography color="primary" variant="overline">
                Vite + React + MUI bootstrap
              </Typography>
              <Typography variant="h1">Theme system is in place.</Typography>
              <Typography color="textSecondary" variant="body1">
                Theme management is now explicit: source, user overrides,
                localStorage persistence, and the resolved MUI theme are split
                into separate responsibilities.
              </Typography>
            </Stack>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <Button component={RouterLink} to="/" variant="contained">
                /
              </Button>
              <Button component={RouterLink} to="/home" variant="outlined">
                /home
              </Button>
            </Stack>

            <Stack spacing={1}>
              <Typography variant="subtitle2">Current theme state</Typography>
              <Typography color="textSecondary" variant="body2">
                Source: {source}
              </Typography>
              <Typography color="textSecondary" variant="body2">
                Stored mode: {mode}
              </Typography>
              <Typography color="textSecondary" variant="body2">
                Stored template: {templateName}
              </Typography>
              <Typography color="textSecondary" variant="body2">
                Active mode: {resolvedMode}
              </Typography>
              <Typography color="textSecondary" variant="body2">
                Active template: {resolvedTemplateName}
              </Typography>
            </Stack>

            <Typography color="textSecondary" variant="body2">
              The widget below can override theme settings at runtime and the
              chosen source and user settings persist in local storage.
            </Typography>

            <Link component={RouterLink} to="/home" variant="body2">
              Home route alias
            </Link>
          </Stack>
          </Paper>
          <ThemeWidget />
        </Stack>
      </Stack>
    </Container>
  );
};
