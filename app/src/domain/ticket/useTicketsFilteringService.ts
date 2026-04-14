import { useCallback, useMemo, useState } from "react";

import type { Ticket } from "./graphql";
import type { TicketStatusKey } from "./useTicketsService";
import type { TicketsServiceSource } from "./useTicketsService";

interface UseTicketsFilteringServiceParams {
  ticketsRef: { current: TicketsServiceSource };
}

export type TicketPrioritySorting = "high_to_low" | "low_to_high";

export interface UseTicketsFilteringServiceResult {
  applyFiltering: () => Ticket[];
  assignedToUserIds: string[];
  columnPriorityDirections: Record<TicketStatusKey, TicketPrioritySorting>;
  selectedPriorities: number[];
  selectedSeverityIds: string[];
  resetFilters: () => void;
  searchKeywords: string;
  setColumnPriorityDirection: (status: TicketStatusKey, value: TicketPrioritySorting) => void;
  setAssignedToUserIds: (value: string[]) => void;
  setSelectedPriorities: (value: number[]) => void;
  setSelectedSeverityIds: (value: string[]) => void;
  setSearchKeywords: (value: string) => void;
}

const DEFAULT_COLUMN_PRIORITY_DIRECTIONS: Record<TicketStatusKey, TicketPrioritySorting> = {
  code_review: "high_to_low",
  done: "high_to_low",
  in_progress: "high_to_low",
  in_testing: "high_to_low",
  new: "high_to_low"
};

export const compareTicketsByPriority = (
  left: Ticket,
  right: Ticket,
  sorting: TicketPrioritySorting
): number => {
  if (left.priority === null && right.priority === null) {
    return left.name.localeCompare(right.name);
  }

  if (left.priority === null) {
    return 1;
  }

  if (right.priority === null) {
    return -1;
  }

  if (left.priority !== right.priority) {
    return sorting === "high_to_low"
      ? left.priority - right.priority
      : right.priority - left.priority;
  }

  return left.name.localeCompare(right.name);
};

export const useTicketsFilteringService = ({
  ticketsRef
}: UseTicketsFilteringServiceParams): UseTicketsFilteringServiceResult => {
  const [searchKeywords, setSearchKeywords] = useState("");
  const [assignedToUserIds, setAssignedToUserIds] = useState<string[]>([]);
  const [selectedSeverityIds, setSelectedSeverityIds] = useState<string[]>([]);
  const [selectedPriorities, setSelectedPriorities] = useState<number[]>([]);
  const [columnPriorityDirections, setColumnPriorityDirections] = useState(DEFAULT_COLUMN_PRIORITY_DIRECTIONS);

  const normalizedSearch = useMemo(() => {
    return searchKeywords.trim().toLowerCase();
  }, [searchKeywords]);

  const hasAssignedToUserIds = assignedToUserIds.length > 0;
  const hasSelectedSeverityIds = selectedSeverityIds.length > 0;
  const hasSelectedPriorities = selectedPriorities.length > 0;
  const hasSearchKeywords = normalizedSearch.length > 0;

  const applyFiltering = useCallback((): Ticket[] => {
    return ticketsRef.current.ids
      .map(ticketId => ticketsRef.current.byId[ticketId])
      .filter((ticket): ticket is Ticket => ticket !== undefined)
      .filter(ticket => {
        const matchesAssignee =
          !hasAssignedToUserIds ||
          (ticket.assignedToUserId !== null && assignedToUserIds.includes(ticket.assignedToUserId));
        const matchesSearch =
          !hasSearchKeywords ||
          [ticket.name, ticket.description, ticket.acceptanceCriteria].some(value => {
            return value?.toLowerCase().includes(normalizedSearch) ?? false;
          });
        const matchesSeverity =
          !hasSelectedSeverityIds ||
          (ticket.severityId !== null && selectedSeverityIds.includes(ticket.severityId));
        const matchesPriority =
          !hasSelectedPriorities ||
          (ticket.priority !== null && selectedPriorities.includes(ticket.priority));

        return matchesAssignee && matchesSearch && matchesSeverity && matchesPriority;
      });
  }, [
    assignedToUserIds,
    hasAssignedToUserIds,
    hasSearchKeywords,
    hasSelectedPriorities,
    hasSelectedSeverityIds,
    normalizedSearch,
    selectedPriorities,
    selectedSeverityIds,
    ticketsRef
  ]);

  return {
    applyFiltering,
    assignedToUserIds,
    columnPriorityDirections,
    selectedPriorities,
    selectedSeverityIds,
    resetFilters: () => {
      setAssignedToUserIds([]);
      setColumnPriorityDirections(DEFAULT_COLUMN_PRIORITY_DIRECTIONS);
      setSelectedPriorities([]);
      setSelectedSeverityIds([]);
      setSearchKeywords("");
    },
    searchKeywords,
    setColumnPriorityDirection: (status, value) => {
      setColumnPriorityDirections(currentDirections => {
        return {
          ...currentDirections,
          [status]: value
        };
      });
    },
    setAssignedToUserIds,
    setSelectedPriorities,
    setSelectedSeverityIds,
    setSearchKeywords
  };
};
