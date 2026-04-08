import type { ReactElement } from "react";
import { useState } from "react";

import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { CreateBoardDialog } from "@components/boards/CreateBoardDialog";
import { NoBoards } from "@components/boards/NoBoards";
import { AppPageLayout } from "@components/layout/AppPageLayout";
import { useBoards } from "@hooks/useBoards";
import { BoardsProvider } from "@providers/BoardsProvider";

const MyBoardsPageContent = (): ReactElement => {
  const [isCreateBoardDialogOpen, setIsCreateBoardDialogOpen] = useState(false);
  const { boards } = useBoards();

  return (
    <AppPageLayout>
      <Stack spacing={1}>
        <Typography variant="h2">My boards</Typography>
        <Typography color="text.secondary" variant="body1">
          Your boards will appear here once you are invited to one or create a
          new board yourself.
        </Typography>
      </Stack>

      {boards.length === 0 ? (
        <NoBoards
          onCreateBoard={() => {
            setIsCreateBoardDialogOpen(true);
          }}
        />
      ) : (
        <Stack spacing={2}>
          {boards.map(board => (
            <Paper key={board.boardId} sx={{ p: 3 }} variant="outlined">
              <Stack spacing={1}>
                <Typography variant="h5">{board.name}</Typography>
                {board.description !== null && board.description.length > 0 ? (
                  <Typography color="text.secondary" variant="body2">
                    {board.description}
                  </Typography>
                ) : null}
              </Stack>
            </Paper>
          ))}
        </Stack>
      )}

      <CreateBoardDialog
        onClose={() => {
          setIsCreateBoardDialogOpen(false);
        }}
        open={isCreateBoardDialogOpen}
      />
    </AppPageLayout>
  );
};

/**
 * Boards entry page backed by the boards facade provider.
 */
export const MyBoardsPage = (): ReactElement => {
  return (
    <BoardsProvider>
      <MyBoardsPageContent />
    </BoardsProvider>
  );
};
