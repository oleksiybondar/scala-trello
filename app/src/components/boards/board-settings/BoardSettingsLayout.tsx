import type { ReactElement } from "react";

import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import { Outlet } from "react-router-dom";

import { BoardSettingsHeader } from "@components/boards/board-settings/BoardSettingsHeader";
import { BoardSettingsSidebar } from "@components/boards/board-settings/BoardSettingsSidebar";
import { boardSettingsNavItems } from "@components/boards/board-settings/types";
import { AppPageLayout } from "@components/layout/AppPageLayout";
import { useBoard } from "@hooks/useBoard";

export const BoardSettingsLayout = (): ReactElement => {
  const { boardPermissionAccess, canManageBoardSettings } = useBoard();
  const visibleItems = boardSettingsNavItems.filter(item =>
    item.isVisible(boardPermissionAccess)
  );

  return (
    <AppPageLayout>
      <BoardSettingsHeader />

      {!canManageBoardSettings || visibleItems.length === 0 ? (
        <Typography color="text.secondary" variant="body1">
          You do not have access to board settings.
        </Typography>
      ) : (
        <Grid container spacing={3}>
          <Grid size={{ md: 4, xs: 12 }}>
            <BoardSettingsSidebar />
          </Grid>

          <Grid size={{ md: 8, xs: 12 }}>
            <Outlet />
          </Grid>
        </Grid>
      )}
    </AppPageLayout>
  );
};
