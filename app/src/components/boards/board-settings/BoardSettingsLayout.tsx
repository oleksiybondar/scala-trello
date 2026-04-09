import type { ReactElement } from "react";

import Grid from "@mui/material/Grid";
import { Outlet } from "react-router-dom";

import { BoardSettingsHeader } from "@components/boards/board-settings/BoardSettingsHeader";
import { BoardSettingsSidebar } from "@components/boards/board-settings/BoardSettingsSidebar";
import { AppPageLayout } from "@components/layout/AppPageLayout";

export const BoardSettingsLayout = (): ReactElement => {
  return (
    <AppPageLayout>
      <BoardSettingsHeader />

      <Grid container spacing={3}>
        <Grid size={{ md: 4, xs: 12 }}>
          <BoardSettingsSidebar />
        </Grid>

        <Grid size={{ md: 8, xs: 12 }}>
          <Outlet />
        </Grid>
      </Grid>
    </AppPageLayout>
  );
};
