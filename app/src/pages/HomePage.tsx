import type { ReactElement } from "react";
import { useState } from "react";

import Button from "@mui/material/Button";
import Container from "@mui/material/Container";
import Link from "@mui/material/Link";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { Link as RouterLink } from "react-router-dom";

import { AppNavBar } from "@components/navigation/AppNavBar";
import { AvatarInput } from "@components/form-elements/avatar/AvatarInput";
import { ThemeWidget } from "@components/theme/ThemeWidget";
import { useAuth } from "@hooks/useAuth";
import { useThemeManager } from "@hooks/useThemeManager";

export const HomePage = (): ReactElement => {
  const { isAuthenticated } = useAuth();
  const { mode, resolvedMode, resolvedTemplateName, source, templateName } =
    useThemeManager();
  const [avatarValue, setAvatarValue] = useState("");

  return (
    <Container>
      <Stack minHeight="100vh" py={4} spacing={4}>
        <AppNavBar />

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
                <Button
                  component={RouterLink}
                  to="/settings/profile"
                  variant="outlined"
                >
                  /settings/profile
                </Button>
                {!isAuthenticated ? (
                  <Button component={RouterLink} to="/login" variant="outlined">
                    /login
                  </Button>
                ) : null}
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

              <Link component={RouterLink} to="/login" variant="body2">
                Go to the login page
              </Link>
            </Stack>
          </Paper>

          <Paper>
            <Stack spacing={3} p={4}>
              <Stack spacing={1}>
                <Typography color="primary" variant="overline">
                  Form primitives
                </Typography>
                <Typography variant="h3">Avatar input prototype</Typography>
                <Typography color="textSecondary" variant="body1">
                  Temporary playground for the local-only avatar picker before
                  it is composed into the actual change-avatar form.
                </Typography>
              </Stack>

              <AvatarInput
                helperText="Uses the native file picker, validates image type and size, and stores the result as a data URL."
                label="Try an avatar"
                onChange={setAvatarValue}
                value={avatarValue}
              />
            </Stack>
          </Paper>
          <ThemeWidget />
        </Stack>
      </Stack>
    </Container>
  );
};
