import type { ReactElement } from "react";

import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { AppPageLayout } from "@components/layout/AppPageLayout";
import { MyTicketsTable } from "@components/tickets/my-tickets/MyTicketsTable";
import { useInfiniteScrollTrigger } from "@hooks/useInfiniteScrollTrigger";
import { MyTicketsToolbar } from "@components/tickets/my-tickets/MyTicketsToolbar";
import { useMyTickets } from "@hooks/useMyTickets";
import { MyTicketsProvider } from "@providers/MyTicketsProvider";

const MyTicketsPageBody = (): ReactElement => {
  const {
    canLoadMoreMyTickets,
    isLoadingMyTickets,
    isLoadingNextMyTicketsPage,
    loadNextMyTicketsPage,
    myTickets,
    myTicketsError
  } = useMyTickets();
  const loadMoreSentinelRef = useInfiniteScrollTrigger({
    canLoadMore: canLoadMoreMyTickets,
    isLoadingMore: isLoadingNextMyTicketsPage,
    onLoadMore: loadNextMyTicketsPage
  });

  return (
    <AppPageLayout>
      <Stack spacing={1.5}>
        <Typography variant="h2">My tickets</Typography>
        <MyTicketsToolbar />
        {myTicketsError !== null ? <Alert severity="error">{myTicketsError.message}</Alert> : null}
        {isLoadingMyTickets ? (
          <Typography color="text.secondary" variant="body2">
            Loading tickets...
          </Typography>
        ) : null}
        {!isLoadingMyTickets && myTicketsError === null ? (
          <Stack spacing={1.5}>
            <MyTicketsTable tickets={myTickets} />
            <Box ref={loadMoreSentinelRef} sx={{ height: 1 }} />
            {canLoadMoreMyTickets ? (
              <Typography color="text.secondary" variant="body2">
                {isLoadingNextMyTicketsPage
                  ? "Loading more tickets..."
                  : "Scroll down to load more tickets"}
              </Typography>
            ) : null}
          </Stack>
        ) : null}
      </Stack>
    </AppPageLayout>
  );
};

export const MyTicketsPage = (): ReactElement => {
  return (
    <MyTicketsProvider>
      <MyTicketsPageBody />
    </MyTicketsProvider>
  );
};
