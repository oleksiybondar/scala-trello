import { useCallback, useMemo, useState } from "react";

import type { Ticket } from "./graphql";
import type { TicketsServiceSource } from "./useTicketsService";

interface UseTicketsFilteringServiceParams {
  ticketsRef: { current: TicketsServiceSource };
}

export interface UseTicketsFilteringServiceResult {
  applyFiltering: () => Ticket[];
  assignedToUserIds: string[];
  resetFilters: () => void;
  searchKeywords: string;
  setAssignedToUserIds: (value: string[]) => void;
  setSearchKeywords: (value: string) => void;
}

export const useTicketsFilteringService = ({
  ticketsRef
}: UseTicketsFilteringServiceParams): UseTicketsFilteringServiceResult => {
  const [searchKeywords, setSearchKeywords] = useState("");
  const [assignedToUserIds, setAssignedToUserIds] = useState<string[]>([]);

  const normalizedSearch = useMemo(() => {
    return searchKeywords.trim().toLowerCase();
  }, [searchKeywords]);

  const hasAssignedToUserIds = assignedToUserIds.length > 0;
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

        return matchesAssignee && matchesSearch;
      });
  }, [assignedToUserIds, hasAssignedToUserIds, hasSearchKeywords, normalizedSearch, ticketsRef]);

  return {
    applyFiltering,
    assignedToUserIds,
    resetFilters: () => {
      setAssignedToUserIds([]);
      setSearchKeywords("");
    },
    searchKeywords,
    setAssignedToUserIds,
    setSearchKeywords
  };
};
