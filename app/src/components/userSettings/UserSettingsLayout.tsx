import type { ReactElement } from "react";

import Grid from "@mui/material/Grid";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { Outlet } from "react-router-dom";

import { AppPageLayout } from "@components/layout/AppPageLayout";
import { UserSettingsSidebar } from "@components/userSettings/UserSettingsSidebar";

export const UserSettingsLayout = (): ReactElement => {
  return (
    <AppPageLayout>
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
    </AppPageLayout>
  );
};
