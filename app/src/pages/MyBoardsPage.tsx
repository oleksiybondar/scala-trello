import type { ReactElement } from "react";
import { useState } from "react";

import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { CreateBoardDialog } from "@components/boards/CreateBoardDialog";
import { NoBoards } from "@components/boards/NoBoards";
import { AppPageLayout } from "@components/layout/AppPageLayout";

/**
 * Empty-state entry page for the user's boards area.
 */
export const MyBoardsPage = (): ReactElement => {
  const [isCreateBoardDialogOpen, setIsCreateBoardDialogOpen] = useState(false);

  return (
    <AppPageLayout>
      <Stack spacing={1}>
        <Typography variant="h2">My boards</Typography>
        <Typography color="text.secondary" variant="body1">
          Your boards will appear here once you are invited to one or create a
          new board yourself.
        </Typography>
      </Stack>

      <NoBoards
        onCreateBoard={() => {
          setIsCreateBoardDialogOpen(true);
        }}
      />

      <CreateBoardDialog
        onClose={() => {
          setIsCreateBoardDialogOpen(false);
        }}
        open={isCreateBoardDialogOpen}
      />
    </AppPageLayout>
  );
};
