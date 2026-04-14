import type { PropsWithChildren, ReactElement } from "react";
import { useEffect, useRef, useState } from "react";

import { TicketsContext } from "@contexts/tickets-context";
import type { TicketsContextValue } from "@contexts/tickets-context";
import { useBoard } from "@hooks/useBoard";
import {
  compareTicketsByPriority,
  useTicketsFilteringService
} from "../domain/ticket/useTicketsFilteringService";
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

const selectAndSortTicketsByStatus = (
  tickets: Ticket[],
  status: string,
  direction: "high_to_low" | "low_to_high"
): Ticket[] => {
  return selectTicketsByStatus(tickets, status).sort((left, right) => {
    return compareTicketsByPriority(left, right, direction);
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
    priority: ticket.priority,
    severityId: ticket.severityId,
    severityName: ticket.severityName,
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
    ticketsState.setNewTickets(
      selectAndSortTicketsByStatus(
        ticketsFilteringService.applyFiltering(),
        "new",
        ticketsFilteringService.columnPriorityDirections.new
      )
    );
  }, [
    bootstrapVersion,
    ticketsFilteringService.applyFiltering,
    ticketsFilteringService.columnPriorityDirections.new,
    ticketsState.newTicketsRevision,
    ticketsState.setNewTickets
  ]);

  useEffect(() => {
    ticketsState.setInProgressTickets(
      selectAndSortTicketsByStatus(
        ticketsFilteringService.applyFiltering(),
        "in_progress",
        ticketsFilteringService.columnPriorityDirections.in_progress
      )
    );
  }, [
    bootstrapVersion,
    ticketsFilteringService.applyFiltering,
    ticketsFilteringService.columnPriorityDirections.in_progress,
    ticketsState.inProgressTicketsRevision,
    ticketsState.setInProgressTickets
  ]);

  useEffect(() => {
    ticketsState.setCodeReviewTickets(
      selectAndSortTicketsByStatus(
        ticketsFilteringService.applyFiltering(),
        "code_review",
        ticketsFilteringService.columnPriorityDirections.code_review
      )
    );
  }, [
    bootstrapVersion,
    ticketsFilteringService.applyFiltering,
    ticketsFilteringService.columnPriorityDirections.code_review,
    ticketsState.codeReviewTicketsRevision,
    ticketsState.setCodeReviewTickets
  ]);

  useEffect(() => {
    ticketsState.setInTestingTickets(
      selectAndSortTicketsByStatus(
        ticketsFilteringService.applyFiltering(),
        "in_testing",
        ticketsFilteringService.columnPriorityDirections.in_testing
      )
    );
  }, [
    bootstrapVersion,
    ticketsFilteringService.applyFiltering,
    ticketsFilteringService.columnPriorityDirections.in_testing,
    ticketsState.inTestingTicketsRevision,
    ticketsState.setInTestingTickets
  ]);

  useEffect(() => {
    ticketsState.setDoneTickets(
      selectAndSortTicketsByStatus(
        ticketsFilteringService.applyFiltering(),
        "done",
        ticketsFilteringService.columnPriorityDirections.done
      )
    );
  }, [
    bootstrapVersion,
    ticketsFilteringService.applyFiltering,
    ticketsFilteringService.columnPriorityDirections.done,
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
    isTransitioningTicketState: ticketsService.isTransitioningTicketState,
    newTickets: ticketsState.newTickets,
    columnPriorityDirections: ticketsFilteringService.columnPriorityDirections,
    reassignTicket: ticketsService.reassignTicket,
    reloadTickets: ticketsService.reloadTickets,
    resetFilters: ticketsFilteringService.resetFilters,
    searchKeywords: ticketsFilteringService.searchKeywords,
    setColumnPriorityDirection: ticketsFilteringService.setColumnPriorityDirection,
    setAssignedToUserIds: ticketsFilteringService.setAssignedToUserIds,
    setSelectedPriorities: ticketsFilteringService.setSelectedPriorities,
    setSelectedSeverityIds: ticketsFilteringService.setSelectedSeverityIds,
    setSearchKeywords: ticketsFilteringService.setSearchKeywords,
    selectedPriorities: ticketsFilteringService.selectedPriorities,
    selectedSeverityIds: ticketsFilteringService.selectedSeverityIds,
    tickets,
    ticketsCount,
    ticketsError: ticketsService.ticketsError,
    transitionTicketState: ticketsService.transitionTicketState,
    updateTicket: ticketsService.updateTicket
  };

  return <TicketsContext.Provider value={value}>{children}</TicketsContext.Provider>;
};
