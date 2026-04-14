import type { ReactElement } from "react";

import Alert from "@mui/material/Alert";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { useQuery } from "@tanstack/react-query";
import { useParams } from "react-router-dom";

import { AppPageLayout } from "@components/layout/AppPageLayout";
import { TicketDetailsLayout } from "@components/tickets/ticket-page/TicketDetailsLayout";
import {
  buildTicketQuery,
  mapTicketResponseToTicket
} from "../domain/ticket/graphql";
import type { TicketQueryResponse } from "../domain/ticket/graphql";
import { requestGraphQL } from "@helpers/requestGraphQL";
import { useAuth } from "@hooks/useAuth";

export const TicketPage = (): ReactElement => {
  const { ticketId } = useParams<{ ticketId: string }>();
  const { accessToken, session } = useAuth();
  const normalizedTicketId = ticketId?.trim() ?? "";
  const ticketQuery = useQuery({
    enabled:
      normalizedTicketId.length > 0 && accessToken !== null && session !== null,
    queryFn: async () => {
      if (normalizedTicketId.length === 0) {
        throw new Error("Ticket id is missing.");
      }
      if (session === null) {
        throw new Error("Authentication context is required.");
      }

      const response = await requestGraphQL<TicketQueryResponse>({
        accessToken,
        document: buildTicketQuery(normalizedTicketId),
        tokenType: session.tokenType
      });

      if (response.ticket === null) {
        throw new Error("Ticket was not found.");
      }

      return mapTicketResponseToTicket(response.ticket);
    },
    queryKey: ["ticket-page", normalizedTicketId]
  });

  return (
    <AppPageLayout>
      <Stack spacing={1.5}>
        <Typography variant="h2">Ticket details</Typography>
        {normalizedTicketId.length === 0 ? (
          <Alert severity="error">Ticket id is missing.</Alert>
        ) : null}
        {ticketQuery.error instanceof Error ? (
          <Alert severity="error">{ticketQuery.error.message}</Alert>
        ) : null}
        {ticketQuery.isLoading ? (
          <Typography color="text.secondary" variant="body2">
            Loading ticket...
          </Typography>
        ) : null}
        {ticketQuery.data !== undefined ? <TicketDetailsLayout ticket={ticketQuery.data} /> : null}
      </Stack>
    </AppPageLayout>
  );
};
