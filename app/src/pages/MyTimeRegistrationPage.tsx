import type { ReactElement } from "react";

import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { AppPageLayout } from "@components/layout/AppPageLayout";
import { MyTimeEntriesTable } from "@components/time-tracking/my-time/MyTimeEntriesTable";
import { MyTimeTrackingToolbar } from "@components/time-tracking/my-time/MyTimeTrackingToolbar";
import { useInfiniteScrollTrigger } from "@hooks/useInfiniteScrollTrigger";
import { useTimeTrackingEntries } from "@hooks/useTimeTrackingEntries";
import { TimeTrackingEntriesProvider } from "@providers/TimeTrackingEntriesProvider";

const MyTimeRegistrationPageBody = (): ReactElement => {
  const {
    canLoadMoreEntries,
    isLoadingEntries,
    isLoadingNextEntriesPage,
    loadNextEntriesPage,
    timeTrackingEntries,
    timeTrackingEntriesError,
  } = useTimeTrackingEntries();
  const loadMoreSentinelRef = useInfiniteScrollTrigger({
    canLoadMore: canLoadMoreEntries,
    isLoadingMore: isLoadingNextEntriesPage,
    onLoadMore: loadNextEntriesPage
  });

  return (
    <AppPageLayout>
      <Stack spacing={1.5}>
        <Typography variant="h2">My time registration</Typography>
        <MyTimeTrackingToolbar />
        {timeTrackingEntriesError !== null ? (
          <Alert severity="error">{timeTrackingEntriesError.message}</Alert>
        ) : null}
        {isLoadingEntries ? (
          <Typography color="text.secondary" variant="body2">
            Loading time entries...
          </Typography>
        ) : (
          <Stack spacing={1}>
            <MyTimeEntriesTable entries={timeTrackingEntries} />
            <Box ref={loadMoreSentinelRef} sx={{ height: 1 }} />
            {canLoadMoreEntries ? (
              <Typography color="text.secondary" variant="body2">
                {isLoadingNextEntriesPage ? "Loading more entries..." : "Scroll down to load more entries"}
              </Typography>
            ) : null}
          </Stack>
        )}
      </Stack>
    </AppPageLayout>
  );
};

export const MyTimeRegistrationPage = (): ReactElement => {
  return (
    <TimeTrackingEntriesProvider>
      <MyTimeRegistrationPageBody />
    </TimeTrackingEntriesProvider>
  );
};
