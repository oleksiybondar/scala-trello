import { createContext } from "react";

import type { DictionarySeverity } from "../domain/dictionaries/graphql";
import type { Ticket } from "../domain/ticket/graphql";

export interface TicketContextValue {
  changeTicketAcceptanceCriteria: (acceptanceCriteria: string | null) => Promise<Ticket>;
  changeTicketDescription: (description: string | null) => Promise<Ticket>;
  changeTicketEstimatedTime: (estimatedMinutes: number | null) => Promise<Ticket>;
  changeTicketPriority: (priority: number | null) => Promise<Ticket>;
  changeTicketSeverity: (severityId: string | null) => Promise<Ticket>;
  changeTicketTitle: (title: string) => Promise<Ticket>;
  hasLoadedSeverities: boolean;
  isLoadingSeverities: boolean;
  isLoadingTicket: boolean;
  isUpdatingTicketAcceptanceCriteria: boolean;
  isUpdatingTicketDescription: boolean;
  isUpdatingTicketEstimatedTime: boolean;
  isUpdatingTicketPriority: boolean;
  isUpdatingTicketSeverity: boolean;
  isUpdatingTicketTitle: boolean;
  severities: DictionarySeverity[];
  ticket: Ticket | null;
  ticketError: Error | null;
  ticketId: string;
}

const missingTicketProvider = (): never => {
  throw new Error("TicketContext is missing its provider.");
};

export const TicketContext = createContext<TicketContextValue>({
  changeTicketAcceptanceCriteria: missingTicketProvider,
  changeTicketDescription: missingTicketProvider,
  changeTicketEstimatedTime: missingTicketProvider,
  changeTicketPriority: missingTicketProvider,
  changeTicketSeverity: missingTicketProvider,
  changeTicketTitle: missingTicketProvider,
  hasLoadedSeverities: false,
  isLoadingSeverities: false,
  isLoadingTicket: false,
  isUpdatingTicketAcceptanceCriteria: false,
  isUpdatingTicketDescription: false,
  isUpdatingTicketEstimatedTime: false,
  isUpdatingTicketPriority: false,
  isUpdatingTicketSeverity: false,
  isUpdatingTicketTitle: false,
  severities: [],
  ticket: null,
  ticketError: null,
  ticketId: ""
});
