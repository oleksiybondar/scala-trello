import { createContext } from "react";

import type { Ticket } from "../domain/ticket/graphql";
import type {
  CreateBoardTicketInput,
  TicketsServiceStatus
} from "../domain/ticket/useTicketsService";

export interface TicketsContextValue {
  assignedToUserIds: string[];
  codeReviewTickets: Ticket[];
  createTicket: (input: CreateBoardTicketInput) => Promise<Ticket>;
  doneTickets: Ticket[];
  inProgressTickets: Ticket[];
  inTestingTickets: Ticket[];
  isCreatingTicket: boolean;
  isReassigningTicket: boolean;
  isReloadingTickets: boolean;
  isTransitioningTicketState: boolean;
  newTickets: Ticket[];
  reassignTicket: (ticketId: string, assignedToUserId: string | null) => Promise<void>;
  reloadTickets: () => Promise<Ticket[]>;
  resetFilters: () => void;
  searchKeywords: string;
  setAssignedToUserIds: (value: string[]) => void;
  setSearchKeywords: (value: string) => void;
  tickets: Ticket[];
  ticketsCount: number;
  ticketsError: Error | null;
  transitionTicketState: (ticketId: string, status: TicketsServiceStatus) => void;
  updateTicket: (ticketId: string, updater: (ticket: Ticket) => Ticket) => void;
}

const missingTicketsProvider = (): never => {
  throw new Error("TicketsContext is missing its provider.");
};

export const TicketsContext = createContext<TicketsContextValue>({
  assignedToUserIds: [],
  codeReviewTickets: [],
  createTicket: missingTicketsProvider,
  doneTickets: [],
  inProgressTickets: [],
  inTestingTickets: [],
  isCreatingTicket: false,
  isReassigningTicket: false,
  isReloadingTickets: false,
  isTransitioningTicketState: false,
  newTickets: [],
  reassignTicket: missingTicketsProvider,
  reloadTickets: missingTicketsProvider,
  resetFilters: missingTicketsProvider,
  searchKeywords: "",
  setAssignedToUserIds: missingTicketsProvider,
  setSearchKeywords: missingTicketsProvider,
  tickets: [],
  ticketsCount: 0,
  ticketsError: null,
  transitionTicketState: missingTicketsProvider,
  updateTicket: missingTicketsProvider
});
