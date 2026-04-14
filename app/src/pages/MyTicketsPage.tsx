import type { ReactElement } from "react";

import Alert from "@mui/material/Alert";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { AppPageLayout } from "@components/layout/AppPageLayout";
import { MyTicketsToolbar } from "@components/tickets/my-tickets/MyTicketsToolbar";
import { useMyTickets } from "@hooks/useMyTickets";
import { MyTicketsProvider } from "@providers/MyTicketsProvider";

const MyTicketsPageBody = (): ReactElement => {
  const { isLoadingMyTickets, myTicketsError } = useMyTickets();

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
        <Typography color="text.secondary" variant="body1">
          This page is a stub. Ticket-focused personal view is coming next.
        </Typography>
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
