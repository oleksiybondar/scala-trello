import type { ReactElement } from "react";
import { useState } from "react";

import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { BoardCard } from "@components/boards/BoardCard";
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
            <BoardCard board={board} key={board.boardId} />
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
