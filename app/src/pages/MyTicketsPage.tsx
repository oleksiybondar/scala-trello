import type { ReactElement } from "react";

import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { AppPageLayout } from "@components/layout/AppPageLayout";

export const MyTicketsPage = (): ReactElement => {
  return (
    <AppPageLayout>
      <Stack spacing={1.5}>
        <Typography variant="h2">My tickets</Typography>
        <Typography color="text.secondary" variant="body1">
          This page is a stub. Ticket-focused personal view is coming next.
        </Typography>
      </Stack>
    </AppPageLayout>
  );
};
