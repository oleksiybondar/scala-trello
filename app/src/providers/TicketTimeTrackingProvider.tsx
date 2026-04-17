import type { PropsWithChildren, ReactElement } from "react";

import { useTickets } from "@hooks/useTickets";
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

export const TicketTimeTrackingProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const { updateTicket } = useTickets();

  return (
    <TimeTrackingProvider
      onRegister={async entry => {
        await updateTicket(entry.ticketId, currentTicket => {
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
      {children}
    </TimeTrackingProvider>
  );
};
