import type { PropsWithChildren, ReactElement } from "react";
import { useEffect, useRef, useState } from "react";

import { TicketsContext } from "@contexts/tickets-context";
import type { TicketsContextValue } from "@contexts/tickets-context";
import { useBoard } from "@hooks/useBoard";
import { useTicketsFilteringService } from "../domain/ticket/useTicketsFilteringService";
import { useTicketsState } from "@providers/tickets/useTicketsState";
import type {
  Board,
  BoardTicket,
  BoardTimeTrackingEntry,
  BoardUserSummary
} from "../domain/board/graphql";
import type {
  Ticket,
  TicketTimeTrackingEntry,
  TicketTimeTrackingTicketSummary,
  TicketUserSummary
} from "../domain/ticket/graphql";
import type { TicketsServiceSource } from "../domain/ticket/useTicketsService";
import { useTicketsService } from "../domain/ticket/useTicketsService";

const selectTicketsByStatus = (tickets: Ticket[], status: string): Ticket[] => {
  return tickets.filter(ticket => {
    return ticket.status?.trim().toLowerCase() === status;
  });
};

const mapUserSummary = (user: BoardUserSummary | null): TicketUserSummary | null => {
  if (user === null) {
    return null;
  }

  return {
    avatarUrl: user.avatarUrl,
    firstName: user.firstName,
    lastName: user.lastName,
    userId: user.userId
  };
};

const mapTimeEntryTicketSummary = (
  summary: NonNullable<BoardTimeTrackingEntry["ticket"]>
): TicketTimeTrackingTicketSummary => {
  return {
    description: summary.description,
    ticketId: summary.ticketId,
    title: summary.title
  };
};

const mapTimeEntry = (entry: BoardTimeTrackingEntry): TicketTimeTrackingEntry => {
  return {
    activityCode: entry.activityCode,
    activityId: entry.activityId,
    activityName: entry.activityName,
    description: entry.description,
    durationMinutes: entry.durationMinutes,
    entryId: entry.entryId,
    loggedAt: entry.loggedAt,
    ticket: entry.ticket === null ? null : mapTimeEntryTicketSummary(entry.ticket),
    ticketId: entry.ticketId,
    user: mapUserSummary(entry.user),
    userId: entry.userId
  };
};

const mapBoardTicketToTicket = (board: Board | null, ticket: BoardTicket): Ticket => {
  return {
    acceptanceCriteria: ticket.acceptanceCriteria,
    assignedTo: mapUserSummary(ticket.assignedTo),
    assignedToUserId: ticket.assignedToUserId,
    board:
      board === null
        ? null
        : {
            active: board.active,
            boardId: board.boardId,
            name: board.name
          },
    boardId: ticket.boardId,
    comments: [],
    commentsCount: ticket.commentsCount,
    createdAt: ticket.createdAt,
    createdBy: mapUserSummary(ticket.createdBy),
    createdByUserId: ticket.createdByUserId,
    description: ticket.description,
    estimatedMinutes: ticket.estimatedMinutes,
    lastModifiedBy: mapUserSummary(ticket.lastModifiedBy),
    lastModifiedByUserId: ticket.lastModifiedByUserId,
    modifiedAt: ticket.modifiedAt,
    name: ticket.name,
    status: ticket.status,
    ticketId: ticket.ticketId,
    timeEntries: ticket.timeEntries.map(mapTimeEntry),
    trackedMinutes: ticket.trackedMinutes
  };
};

const toTicketsSource = (tickets: Ticket[]): TicketsServiceSource => {
  return {
    byId: Object.fromEntries(tickets.map(ticket => [ticket.ticketId, ticket])),
    ids: tickets.map(ticket => ticket.ticketId)
  };
};

export const TicketsProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const { board } = useBoard();
  const ticketsRef = useRef<TicketsServiceSource>({
    byId: {},
    ids: []
  });
  const [bootstrapVersion, setBootstrapVersion] = useState(0);
  const boardId = board?.boardId ?? "";
  const ticketsState = useTicketsState();
  const ticketsService = useTicketsService({
    boardId,
    ticketsRef,
    updateVersioning: ticketsState.updateVersioning
  });
  const ticketsFilteringService = useTicketsFilteringService({
    ticketsRef
  });

  useEffect(() => {
    ticketsRef.current = toTicketsSource(
      (board?.tickets ?? []).map(ticket => mapBoardTicketToTicket(board ?? null, ticket))
    );
    setBootstrapVersion(version => version + 1);
  }, [board]);

  useEffect(() => {
    ticketsState.setNewTickets(selectTicketsByStatus(ticketsFilteringService.applyFiltering(), "new"));
  }, [
    bootstrapVersion,
    ticketsFilteringService.applyFiltering,
    ticketsState.newTicketsRevision,
    ticketsState.setNewTickets
  ]);

  useEffect(() => {
    ticketsState.setInProgressTickets(
      selectTicketsByStatus(ticketsFilteringService.applyFiltering(), "in progress")
    );
  }, [
    bootstrapVersion,
    ticketsFilteringService.applyFiltering,
    ticketsState.inProgressTicketsRevision,
    ticketsState.setInProgressTickets
  ]);

  useEffect(() => {
    ticketsState.setCodeReviewTickets(
      selectTicketsByStatus(ticketsFilteringService.applyFiltering(), "code review")
    );
  }, [
    bootstrapVersion,
    ticketsFilteringService.applyFiltering,
    ticketsState.codeReviewTicketsRevision,
    ticketsState.setCodeReviewTickets
  ]);

  useEffect(() => {
    ticketsState.setInTestingTickets(
      selectTicketsByStatus(ticketsFilteringService.applyFiltering(), "in testing")
    );
  }, [
    bootstrapVersion,
    ticketsFilteringService.applyFiltering,
    ticketsState.inTestingTicketsRevision,
    ticketsState.setInTestingTickets
  ]);

  useEffect(() => {
    ticketsState.setDoneTickets(selectTicketsByStatus(ticketsFilteringService.applyFiltering(), "done"));
  }, [
    bootstrapVersion,
    ticketsFilteringService.applyFiltering,
    ticketsState.doneTicketsRevision,
    ticketsState.setDoneTickets
  ]);

  const ticketsCount = ticketsRef.current.ids.length;
  const tickets = ticketsService.getAllTickets();

  const value: TicketsContextValue = {
    assignedToUserIds: ticketsFilteringService.assignedToUserIds,
    codeReviewTickets: ticketsState.codeReviewTickets,
    createTicket: ticketsService.createTicket,
    doneTickets: ticketsState.doneTickets,
    inProgressTickets: ticketsState.inProgressTickets,
    inTestingTickets: ticketsState.inTestingTickets,
    isCreatingTicket: ticketsService.isCreatingTicket,
    isReassigningTicket: ticketsService.isReassigningTicket,
    isReloadingTickets: ticketsService.isReloadingTickets,
    isTransitioningTicketState: false,
    newTickets: ticketsState.newTickets,
    reassignTicket: ticketsService.reassignTicket,
    reloadTickets: ticketsService.reloadTickets,
    resetFilters: ticketsFilteringService.resetFilters,
    searchKeywords: ticketsFilteringService.searchKeywords,
    setAssignedToUserIds: ticketsFilteringService.setAssignedToUserIds,
    setSearchKeywords: ticketsFilteringService.setSearchKeywords,
    tickets,
    ticketsCount,
    ticketsError: ticketsService.ticketsError,
    transitionTicketState: ticketsService.transitionTicketState,
    updateTicket: ticketsService.updateTicket
  };

  return <TicketsContext.Provider value={value}>{children}</TicketsContext.Provider>;
};
