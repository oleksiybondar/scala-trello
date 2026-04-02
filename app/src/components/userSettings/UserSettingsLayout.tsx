import type { ReactElement } from "react";

import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { Outlet } from "react-router-dom";

import { AppNavBar } from "@components/navigation/AppNavBar";
import { UserSettingsSidebar } from "@components/userSettings/UserSettingsSidebar";

export const UserSettingsLayout = (): ReactElement => {
  return (
    <Container maxWidth="lg">
      <Stack minHeight="100vh" py={4} spacing={4}>
        <AppNavBar />

        <Stack spacing={1}>
          <Typography variant="h2">User settings</Typography>
          <Typography color="textSecondary" variant="body1">
            Manage profile data, account security, and UI preferences.
          </Typography>
        </Stack>

        <Grid container spacing={3}>
          <Grid size={{ md: 4, xs: 12 }}>
            <UserSettingsSidebar />
          </Grid>

          <Grid size={{ md: 8, xs: 12 }}>
            <Outlet />
          </Grid>
        </Grid>
      </Stack>
    </Container>
  );
};
