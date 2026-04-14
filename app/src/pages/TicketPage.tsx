import type { ReactElement } from "react";

import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { useParams } from "react-router-dom";

import { AppPageLayout } from "@components/layout/AppPageLayout";

export const TicketPage = (): ReactElement => {
  const { ticketId } = useParams<{ ticketId: string }>();

  return (
    <AppPageLayout>
      <Stack spacing={1.5}>
        <Typography variant="h2">Ticket</Typography>
        <Typography color="text.secondary" variant="body1">
          {ticketId === undefined || ticketId.trim().length === 0
            ? "Ticket id is missing."
            : `Ticket id: ${ticketId}`}
        </Typography>
      </Stack>
    </AppPageLayout>
  );
};
