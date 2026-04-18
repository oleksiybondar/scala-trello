import type { ReactElement } from "react";

import Alert from "@mui/material/Alert";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { AppPageLayout } from "@components/layout/AppPageLayout";
import { MyTimeTrackingToolbar } from "@components/time-tracking/my-time/MyTimeTrackingToolbar";
import { useTimeTrackingEntries } from "@hooks/useTimeTrackingEntries";
import { TimeTrackingEntriesProvider } from "@providers/TimeTrackingEntriesProvider";

const MyTimeRegistrationPageBody = (): ReactElement => {
  const {
    isLoadingEntries,
    timeTrackingEntries,
    timeTrackingEntriesError,
  } = useTimeTrackingEntries();

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
          <Typography color="text.secondary" variant="body2">
            Loaded {String(timeTrackingEntries.length)} entries. Next step is toolbar + editable table UI.
          </Typography>
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
