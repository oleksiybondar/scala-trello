import { useCallback, useEffect, useMemo, useRef } from "react";

import { useInfiniteQuery } from "@tanstack/react-query";
import type { InfiniteData } from "@tanstack/react-query";

import type { NormalizedQueryMyTicketsParams } from "@contexts/my-tickets-context";
import { requestGraphQL } from "@helpers/requestGraphQL";
import { useAuth } from "@hooks/useAuth";
import {
  buildMyTicketsQuery,
  mapTicketResponseToTicket
} from "./graphql";
import type {
  MyTicketsQueryResponse,
  Ticket
} from "./graphql";
import { countMyTickets, filterMyTickets } from "./myTicketsApi";

interface UseMyTicketsServiceParams {
  currentParams: NormalizedQueryMyTicketsParams;
}

interface UseMyTicketsServiceResult {
  canLoadMoreMyTickets: boolean;
  isLoadingMyTickets: boolean;
  isLoadingNextMyTicketsPage: boolean;
  loadNextMyTicketsPage: () => Promise<void>;
  myTickets: Ticket[];
  myTicketsError: Error | null;
  totalMyTickets: number;
}

export const useMyTicketsService = ({
  currentParams
}: UseMyTicketsServiceParams): UseMyTicketsServiceResult => {
  const MY_TICKETS_PER_PAGE = 30;
  const { accessToken, session } = useAuth();
  const isEnabled = accessToken !== null && session !== null;
  const isLoadingMoreRef = useRef(false);

  const ticketsQuery = useInfiniteQuery<
    Ticket[],
    Error,
    InfiniteData<Ticket[]>,
    readonly unknown[],
    number
  >({
    enabled: isEnabled,
    getNextPageParam: (lastPage, allPages) => {
      if (lastPage.length < MY_TICKETS_PER_PAGE) {
        return undefined;
      }

      return allPages.reduce((total, page) => total + page.length, 0);
    },
    initialPageParam: 0,
    queryFn: async ({ pageParam }): Promise<Ticket[]> => {
      if (accessToken === null || session === null) {
        throw new Error("Authentication context is required for ticket operations.");
      }

      const response = await requestGraphQL<MyTicketsQueryResponse>({
        accessToken,
        document: buildMyTicketsQuery(
          currentParams.assignedOnly,
          pageParam,
          MY_TICKETS_PER_PAGE
        ),
        tokenType: session.tokenType
      });

      return response.myTickets.map(mapTicketResponseToTicket);
    },
    queryKey: [
      "my-tickets",
      currentParams.assignedOnly ? "assigned-only" : "assigned-or-created",
      currentParams.keyword ?? "",
      currentParams.priorities.join(","),
      currentParams.severityIds.join(",")
    ]
  });

  const allTickets = useMemo(() => {
    return ticketsQuery.data?.pages.flatMap(page => page) ?? [];
  }, [ticketsQuery.data?.pages]);
  const myTickets = useMemo(() => {
    return filterMyTickets(allTickets, currentParams);
  }, [allTickets, currentParams]);
  const totalMyTickets = useMemo(() => {
    return countMyTickets(allTickets, currentParams);
  }, [allTickets, currentParams]);

  useEffect(() => {
    isLoadingMoreRef.current = ticketsQuery.isFetchingNextPage;
  }, [ticketsQuery.isFetchingNextPage]);

  const loadNextMyTicketsPage = useCallback(async () => {
    if (isLoadingMoreRef.current || !ticketsQuery.hasNextPage) {
      return;
    }

    isLoadingMoreRef.current = true;

    try {
      await ticketsQuery.fetchNextPage();
    } finally {
      isLoadingMoreRef.current = false;
    }
  }, [ticketsQuery.fetchNextPage, ticketsQuery.hasNextPage]);

  return {
    canLoadMoreMyTickets: ticketsQuery.hasNextPage,
    isLoadingMyTickets: ticketsQuery.isLoading,
    isLoadingNextMyTicketsPage: ticketsQuery.isFetchingNextPage,
    loadNextMyTicketsPage,
    myTickets,
    myTicketsError: ticketsQuery.error instanceof Error ? ticketsQuery.error : null,
    totalMyTickets
  };
};
