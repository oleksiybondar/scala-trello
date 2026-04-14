import type { ReactElement } from "react";

import Alert from "@mui/material/Alert";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { AppPageLayout } from "@components/layout/AppPageLayout";
import { MyTicketsTable } from "@components/tickets/my-tickets/MyTicketsTable";
import { MyTicketsToolbar } from "@components/tickets/my-tickets/MyTicketsToolbar";
import { useMyTickets } from "@hooks/useMyTickets";
import { MyTicketsProvider } from "@providers/MyTicketsProvider";

const MyTicketsPageBody = (): ReactElement => {
  const { isLoadingMyTickets, myTickets, myTicketsError } = useMyTickets();

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
        {!isLoadingMyTickets && myTicketsError === null ? <MyTicketsTable tickets={myTickets} /> : null}
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
