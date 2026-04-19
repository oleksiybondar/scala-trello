import type { ReactElement } from "react";
import { useState } from "react";

import Box from "@mui/material/Box";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { BoardCard } from "@components/boards/BoardCard";
import { BoardsToolbar } from "@components/boards/boards-toolbar/BoardsToolbar";
import { CreateBoardDialog } from "@components/boards/CreateBoardDialog";
import { NoBoards } from "@components/boards/NoBoards";
import { NoBoardsFound } from "@components/boards/NoBoardsFound";
import { AppPageLayout } from "@components/layout/AppPageLayout";
import { useBoards } from "@hooks/useBoards";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { useInfiniteScrollTrigger } from "@hooks/useInfiniteScrollTrigger";
import { BoardsProvider } from "@providers/BoardsProvider";

const MyBoardsPageBody = (): ReactElement => {
  const [isCreateBoardDialogOpen, setIsCreateBoardDialogOpen] = useState(false);
  const {
    boards,
    canLoadMoreBoards,
    currentParams,
    isLoadingMoreBoards,
    loadNextBoardsPage
  } = useBoards();
  const { userId } = useCurrentUser();

  const hasKeywordFilter = (currentParams.keyword ?? "").trim().length > 0;
  const isMeOwnerFilter = userId !== null && currentParams.owner === userId;
  const shouldShowInitialEmptyState =
    !hasKeywordFilter &&
    (currentParams.owner === undefined || isMeOwnerFilter);
  const loadMoreSentinelRef = useInfiniteScrollTrigger({
    canLoadMore: canLoadMoreBoards,
    isLoadingMore: isLoadingMoreBoards,
    onLoadMore: loadNextBoardsPage
  });

  return (
    <AppPageLayout>
      <Stack spacing={1.5}>
        <Typography variant="h2">My boards</Typography>
        <BoardsToolbar
          onCreateBoard={() => {
            setIsCreateBoardDialogOpen(true);
          }}
        />
      </Stack>

      {boards.length === 0 ? (
        shouldShowInitialEmptyState ? (
          <NoBoards
            onCreateBoard={() => {
              setIsCreateBoardDialogOpen(true);
            }}
          />
        ) : (
          <NoBoardsFound />
        )
      ) : (
        <Stack spacing={2}>
          {boards.map(board => (
            <BoardCard board={board} key={board.boardId} />
          ))}
          <Box ref={loadMoreSentinelRef} sx={{ height: 1 }} />
          {canLoadMoreBoards ? (
            <Typography color="text.secondary" variant="body2">
              {isLoadingMoreBoards ? "Loading more boards..." : "Scroll down to load more boards"}
            </Typography>
          ) : null}
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

export const MyBoardsPage = (): ReactElement => {
  return (
    <BoardsProvider>
      <MyBoardsPageBody />
    </BoardsProvider>
  );
};
