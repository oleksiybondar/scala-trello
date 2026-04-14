import { useMemo } from "react";

import { useQuery } from "@tanstack/react-query";

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
  isLoadingMyTickets: boolean;
  myTickets: Ticket[];
  myTicketsError: Error | null;
  totalMyTickets: number;
}

export const useMyTicketsService = ({
  currentParams
}: UseMyTicketsServiceParams): UseMyTicketsServiceResult => {
  const { accessToken, session } = useAuth();
  const isEnabled = accessToken !== null && session !== null;

  const ticketsQuery = useQuery({
    enabled: isEnabled,
    queryFn: async (): Promise<Ticket[]> => {
      if (accessToken === null || session === null) {
        throw new Error("Authentication context is required for ticket operations.");
      }

      const response = await requestGraphQL<MyTicketsQueryResponse>({
        accessToken,
        document: buildMyTicketsQuery(),
        tokenType: session.tokenType
      });

      return response.myTickets.map(mapTicketResponseToTicket);
    },
    queryKey: [
      "my-tickets",
      currentParams.keyword ?? "",
      currentParams.page,
      currentParams.priorities.join(","),
      currentParams.severityIds.join(",")
    ]
  });

  const myTickets = useMemo(() => {
    return filterMyTickets(ticketsQuery.data ?? [], currentParams);
  }, [currentParams, ticketsQuery.data]);
  const totalMyTickets = useMemo(() => {
    return countMyTickets(ticketsQuery.data ?? [], currentParams);
  }, [currentParams, ticketsQuery.data]);

  return {
    isLoadingMyTickets: ticketsQuery.isLoading,
    myTickets,
    myTicketsError: ticketsQuery.error instanceof Error ? ticketsQuery.error : null,
    totalMyTickets
  };
};
