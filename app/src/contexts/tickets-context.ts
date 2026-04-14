import { createContext } from "react";

import type { Ticket } from "../domain/ticket/graphql";
import type {
  CreateBoardTicketInput,
  TicketStatusKey,
  TicketsServiceStatus
} from "../domain/ticket/useTicketsService";
import type { TicketPrioritySorting } from "../domain/ticket/useTicketsFilteringService";

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
  columnPriorityDirections: Record<TicketStatusKey, TicketPrioritySorting>;
  reassignTicket: (ticketId: string, assignedToUserId: string | null) => Promise<void>;
  reloadTickets: () => Promise<Ticket[]>;
  resetFilters: () => void;
  searchKeywords: string;
  setColumnPriorityDirection: (status: TicketStatusKey, value: TicketPrioritySorting) => void;
  setAssignedToUserIds: (value: string[]) => void;
  setSelectedPriorities: (value: number[]) => void;
  setSelectedSeverityIds: (value: string[]) => void;
  setSearchKeywords: (value: string) => void;
  selectedPriorities: number[];
  selectedSeverityIds: string[];
  tickets: Ticket[];
  ticketsCount: number;
  ticketsError: Error | null;
  transitionTicketState: (ticketId: string, status: TicketsServiceStatus) => Promise<void>;
  updateTicket: (ticketId: string, updater: (ticket: Ticket) => Ticket) => Promise<void>;
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
  columnPriorityDirections: {
    code_review: "high_to_low",
    done: "high_to_low",
    in_progress: "high_to_low",
    in_testing: "high_to_low",
    new: "high_to_low"
  },
  reassignTicket: missingTicketsProvider,
  reloadTickets: missingTicketsProvider,
  resetFilters: missingTicketsProvider,
  searchKeywords: "",
  setColumnPriorityDirection: missingTicketsProvider,
  setAssignedToUserIds: missingTicketsProvider,
  setSelectedPriorities: missingTicketsProvider,
  setSelectedSeverityIds: missingTicketsProvider,
  setSearchKeywords: missingTicketsProvider,
  selectedPriorities: [],
  selectedSeverityIds: [],
  tickets: [],
  ticketsCount: 0,
  ticketsError: null,
  transitionTicketState: missingTicketsProvider,
  updateTicket: missingTicketsProvider
});
