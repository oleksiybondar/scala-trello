import type { ReactElement } from "react";

import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { BoardMainArea } from "@components/boards/board-page/BoardMainArea";
import { BoardPageHeader } from "@components/boards/board-page/BoardPageHeader";
import { BoardPageToolbar } from "@components/boards/board-page/BoardPageToolbar";
import { AppPageLayout } from "@components/layout/AppPageLayout";
import { useBoard } from "@hooks/useBoard";
import { BoardProvider } from "@providers/BoardProvider";
import { TimeTrackingProvider } from "@providers/TimeTrackingProvider";
import { TicketsProvider } from "@providers/TicketsProvider";

const BoardPageBody = (): ReactElement => {
  const { boardError, isLoadingBoard } = useBoard();

  return (
    <AppPageLayout containerMaxWidth={false}>
      {boardError !== null ? <Alert severity="error">{boardError.message}</Alert> : null}

      {isLoadingBoard ? (
        <Typography color="text.secondary" variant="body1">
          Loading board...
        </Typography>
      ) : null}

      <Stack spacing={3}>
        <BoardPageHeader />
        <Box
          sx={{
            position: "sticky",
            top: { xs: 64, sm: 64 },
            zIndex: theme => theme.zIndex.appBar - 1
          }}
        >
          <BoardPageToolbar />
        </Box>
        <BoardMainArea />
      </Stack>
    </AppPageLayout>
  );
};

export const BoardPage = (): ReactElement => {
  return (
    <BoardProvider>
      <TimeTrackingProvider>
        <TicketsProvider>
          <BoardPageBody />
        </TicketsProvider>
      </TimeTrackingProvider>
    </BoardProvider>
  );
};
