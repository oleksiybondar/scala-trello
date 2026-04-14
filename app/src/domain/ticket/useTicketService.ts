import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { requestGraphQL } from "@helpers/requestGraphQL";
import { useAuth } from "@hooks/useAuth";
import {
  buildChangeTicketAcceptanceCriteriaMutation,
  buildChangeTicketDescriptionMutation,
  buildChangeTicketEstimatedTimeMutation,
  buildChangeTicketPriorityMutation,
  buildChangeTicketSeverityMutation,
  buildChangeTicketTitleMutation,
  buildTicketQuery,
  mapTicketResponseToTicket
} from "./graphql";
import type {
  ChangeTicketAcceptanceCriteriaMutationResponse,
  ChangeTicketDescriptionMutationResponse,
  ChangeTicketEstimatedTimeMutationResponse,
  ChangeTicketPriorityMutationResponse,
  ChangeTicketSeverityMutationResponse,
  ChangeTicketTitleMutationResponse,
  Ticket,
  TicketQueryResponse
} from "./graphql";

interface UseTicketServiceParams {
  ticketId: string;
}

interface UseTicketServiceResult {
  changeTicketAcceptanceCriteria: (acceptanceCriteria: string | null) => Promise<Ticket>;
  changeTicketDescription: (description: string | null) => Promise<Ticket>;
  changeTicketEstimatedTime: (estimatedMinutes: number | null) => Promise<Ticket>;
  changeTicketPriority: (priority: number | null) => Promise<Ticket>;
  changeTicketSeverity: (severityId: string | null) => Promise<Ticket>;
  changeTicketTitle: (title: string) => Promise<Ticket>;
  isLoadingTicket: boolean;
  isUpdatingTicketAcceptanceCriteria: boolean;
  isUpdatingTicketDescription: boolean;
  isUpdatingTicketEstimatedTime: boolean;
  isUpdatingTicketPriority: boolean;
  isUpdatingTicketSeverity: boolean;
  isUpdatingTicketTitle: boolean;
  ticket: Ticket | null;
  ticketError: Error | null;
}

const createTicketQueryKey = (ticketId: string): [string, string] => ["ticket", ticketId];

const loadTicket = async (
  accessToken: string,
  tokenType: string,
  ticketId: string
): Promise<Ticket | null> => {
  const response = await requestGraphQL<TicketQueryResponse>({
    accessToken,
    document: buildTicketQuery(ticketId),
    tokenType
  });

  return response.ticket === null ? null : mapTicketResponseToTicket(response.ticket);
};

export const useTicketService = ({
  ticketId
}: UseTicketServiceParams): UseTicketServiceResult => {
  const { accessToken, session } = useAuth();
  const queryClient = useQueryClient();
  const isEnabled = accessToken !== null && session !== null && ticketId.length > 0;
  const ticketQuery = useQuery({
    enabled: isEnabled,
    queryFn: async () => {
      if (accessToken === null || session === null) {
        throw new Error("Authentication context is required for ticket operations.");
      }

      return loadTicket(accessToken, session.tokenType, ticketId);
    },
    queryKey: createTicketQueryKey(ticketId)
  });

  const updateCachedTicket = (nextTicket: Ticket): void => {
    queryClient.setQueryData(createTicketQueryKey(ticketId), nextTicket);
  };

  const changeTicketTitleMutation = useMutation({
    mutationFn: async (title: string) => {
      if (session === null) {
        throw new Error("Authentication context is required for ticket operations.");
      }

      const response = await requestGraphQL<ChangeTicketTitleMutationResponse>({
        accessToken,
        document: buildChangeTicketTitleMutation(ticketId, title),
        tokenType: session.tokenType
      });

      return mapTicketResponseToTicket(response.changeTicketTitle);
    },
    onSuccess: nextTicket => {
      updateCachedTicket(nextTicket);
    }
  });

  const changeTicketPriorityMutation = useMutation({
    mutationFn: async (priority: number | null) => {
      if (session === null) {
        throw new Error("Authentication context is required for ticket operations.");
      }

      const response = await requestGraphQL<ChangeTicketPriorityMutationResponse>({
        accessToken,
        document: buildChangeTicketPriorityMutation(ticketId, priority),
        tokenType: session.tokenType
      });

      return mapTicketResponseToTicket(response.changeTicketPriority);
    },
    onSuccess: nextTicket => {
      updateCachedTicket(nextTicket);
    }
  });

  const changeTicketEstimatedTimeMutation = useMutation({
    mutationFn: async (estimatedMinutes: number | null) => {
      if (session === null) {
        throw new Error("Authentication context is required for ticket operations.");
      }

      const response = await requestGraphQL<ChangeTicketEstimatedTimeMutationResponse>({
        accessToken,
        document: buildChangeTicketEstimatedTimeMutation(ticketId, estimatedMinutes),
        tokenType: session.tokenType
      });

      return mapTicketResponseToTicket(response.changeTicketEstimatedTime);
    },
    onSuccess: nextTicket => {
      updateCachedTicket(nextTicket);
    }
  });

  const changeTicketSeverityMutation = useMutation({
    mutationFn: async (severityId: string | null) => {
      if (session === null) {
        throw new Error("Authentication context is required for ticket operations.");
      }

      const response = await requestGraphQL<ChangeTicketSeverityMutationResponse>({
        accessToken,
        document: buildChangeTicketSeverityMutation(ticketId, severityId),
        tokenType: session.tokenType
      });

      return mapTicketResponseToTicket(response.changeTicketSeverity);
    },
    onSuccess: nextTicket => {
      updateCachedTicket(nextTicket);
    }
  });

  const changeTicketDescriptionMutation = useMutation({
    mutationFn: async (description: string | null) => {
      if (session === null) {
        throw new Error("Authentication context is required for ticket operations.");
      }

      const response = await requestGraphQL<ChangeTicketDescriptionMutationResponse>({
        accessToken,
        document: buildChangeTicketDescriptionMutation(ticketId, description),
        tokenType: session.tokenType
      });

      return mapTicketResponseToTicket(response.changeTicketDescription);
    },
    onSuccess: nextTicket => {
      updateCachedTicket(nextTicket);
    }
  });

  const changeTicketAcceptanceCriteriaMutation = useMutation({
    mutationFn: async (acceptanceCriteria: string | null) => {
      if (session === null) {
        throw new Error("Authentication context is required for ticket operations.");
      }

      const response = await requestGraphQL<ChangeTicketAcceptanceCriteriaMutationResponse>({
        accessToken,
        document: buildChangeTicketAcceptanceCriteriaMutation(ticketId, acceptanceCriteria),
        tokenType: session.tokenType
      });

      return mapTicketResponseToTicket(response.changeTicketAcceptanceCriteria);
    },
    onSuccess: nextTicket => {
      updateCachedTicket(nextTicket);
    }
  });

  return {
    changeTicketAcceptanceCriteria: async (acceptanceCriteria: string | null) => {
      return changeTicketAcceptanceCriteriaMutation.mutateAsync(acceptanceCriteria);
    },
    changeTicketDescription: async (description: string | null) => {
      return changeTicketDescriptionMutation.mutateAsync(description);
    },
    changeTicketEstimatedTime: async (estimatedMinutes: number | null) => {
      return changeTicketEstimatedTimeMutation.mutateAsync(estimatedMinutes);
    },
    changeTicketPriority: async (priority: number | null) => {
      return changeTicketPriorityMutation.mutateAsync(priority);
    },
    changeTicketSeverity: async (severityId: string | null) => {
      return changeTicketSeverityMutation.mutateAsync(severityId);
    },
    changeTicketTitle: async (title: string) => {
      return changeTicketTitleMutation.mutateAsync(title);
    },
    isLoadingTicket: ticketQuery.isLoading,
    isUpdatingTicketAcceptanceCriteria: changeTicketAcceptanceCriteriaMutation.isPending,
    isUpdatingTicketDescription: changeTicketDescriptionMutation.isPending,
    isUpdatingTicketEstimatedTime: changeTicketEstimatedTimeMutation.isPending,
    isUpdatingTicketPriority: changeTicketPriorityMutation.isPending,
    isUpdatingTicketSeverity: changeTicketSeverityMutation.isPending,
    isUpdatingTicketTitle: changeTicketTitleMutation.isPending,
    ticket: ticketQuery.data ?? null,
    ticketError: ticketQuery.error instanceof Error ? ticketQuery.error : null
  };
};
