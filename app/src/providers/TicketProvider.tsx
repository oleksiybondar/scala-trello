import type { PropsWithChildren, ReactElement } from "react";
import { useEffect } from "react";
import { useParams } from "react-router-dom";

import { TicketContext } from "@contexts/ticket-context";
import { useAuth } from "@hooks/useAuth";
import { useSeverities } from "@hooks/useSeverities";
import { useTicketService } from "../domain/ticket/useTicketService";

export const TicketProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const { ticketId = "" } = useParams();
  const normalizedTicketId = ticketId.trim();
  const { isAuthenticated } = useAuth();
  const {
    hasLoadedSeverities,
    isLoadingSeverities,
    loadSeverities,
    severities
  } = useSeverities();
  const {
    changeTicketAcceptanceCriteria,
    changeTicketDescription,
    changeTicketEstimatedTime,
    changeTicketPriority,
    changeTicketSeverity,
    changeTicketTitle,
    isLoadingTicket,
    isUpdatingTicketAcceptanceCriteria,
    isUpdatingTicketDescription,
    isUpdatingTicketEstimatedTime,
    isUpdatingTicketPriority,
    isUpdatingTicketSeverity,
    isUpdatingTicketTitle,
    ticket,
    ticketError,
    updateTicket
  } = useTicketService({
    ticketId: normalizedTicketId
  });

  useEffect(() => {
    if (!isAuthenticated || hasLoadedSeverities || isLoadingSeverities) {
      return;
    }

    void loadSeverities();
  }, [hasLoadedSeverities, isAuthenticated, isLoadingSeverities, loadSeverities]);

  return (
    <TicketContext.Provider
      value={{
        changeTicketAcceptanceCriteria,
        changeTicketDescription,
        changeTicketEstimatedTime,
        changeTicketPriority,
        changeTicketSeverity,
        changeTicketTitle,
        hasLoadedSeverities,
        isLoadingSeverities,
        isLoadingTicket,
        isUpdatingTicketAcceptanceCriteria,
        isUpdatingTicketDescription,
        isUpdatingTicketEstimatedTime,
        isUpdatingTicketPriority,
        isUpdatingTicketSeverity,
        isUpdatingTicketTitle,
        severities,
        ticket,
        ticketError,
        ticketId: normalizedTicketId,
        updateTicket
      }}
    >
      {children}
    </TicketContext.Provider>
  );
};
