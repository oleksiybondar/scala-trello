import type { ReactElement } from "react";

import Alert from "@mui/material/Alert";
import Link from "@mui/material/Link";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { Link as RouterLink } from "react-router-dom";

import { AppPageLayout } from "@components/layout/AppPageLayout";
import { TicketDetailsLayout } from "@components/tickets/ticket-page/TicketDetailsLayout";
import { useTicket } from "@hooks/useTicket";
import { TicketProvider } from "@providers/TicketProvider";
import { TimeTrackingProvider } from "@providers/TimeTrackingProvider";
import type { TimeTrackingEntry } from "../domain/time-tracking/graphql";
import type { TicketTimeTrackingEntry } from "../domain/ticket/graphql";

const mapEntryToTicketTimeEntry = (entry: TimeTrackingEntry): TicketTimeTrackingEntry => {
  return {
    activityCode: entry.activityCode,
    activityId: entry.activityId,
    activityName: entry.activityName,
    description: entry.description,
    durationMinutes: entry.durationMinutes,
    entryId: entry.entryId,
    loggedAt: entry.loggedAt,
    ticket: entry.ticket,
    ticketId: entry.ticketId,
    user: entry.user,
    userId: entry.userId
  };
};

const TicketPageBody = (): ReactElement => {
  const { isLoadingTicket, ticket, ticketError, ticketId } = useTicket();

  return (
    <AppPageLayout>
      <Stack spacing={1.5}>
        <Typography variant="h2">
          {ticket === null ? "Ticket details" : `[${ticket.ticketId}] ${ticket.name}`}
        </Typography>
        {ticket !== null ? (
          <Link
            color="text.secondary"
            component={RouterLink}
            to={`/boards/${ticket.boardId}`}
            underline="hover"
            variant="body2"
          >
            {ticket.board === null ? `Board ${ticket.boardId}` : `Board: ${ticket.board.name}`}
          </Link>
        ) : null}
        {ticketId.length === 0 ? (
          <Alert severity="error">Ticket id is missing.</Alert>
        ) : null}
        {ticketError !== null ? <Alert severity="error">{ticketError.message}</Alert> : null}
        {isLoadingTicket ? (
          <Typography color="text.secondary" variant="body2">
            Loading ticket...
          </Typography>
        ) : null}
        {ticket !== null ? <TicketDetailsLayout ticket={ticket} /> : null}
      </Stack>
    </AppPageLayout>
  );
};

const TicketPageWithTimeTracking = (): ReactElement => {
  const { updateTicket } = useTicket();

  return (
    <TimeTrackingProvider
      onRegister={async entry => {
        await updateTicket(currentTicket => {
          if (currentTicket.ticketId !== entry.ticketId) {
            return currentTicket;
          }

          if (currentTicket.timeEntries.some(timeEntry => timeEntry.entryId === entry.entryId)) {
            return currentTicket;
          }

          return {
            ...currentTicket,
            timeEntries: [...currentTicket.timeEntries, mapEntryToTicketTimeEntry(entry)],
            trackedMinutes: currentTicket.trackedMinutes + entry.durationMinutes
          };
        });
      }}
    >
      <TicketPageBody />
    </TimeTrackingProvider>
  );
};

export const TicketPage = (): ReactElement => {
  return (
    <TicketProvider>
      <TicketPageWithTimeTracking />
    </TicketProvider>
  );
};
