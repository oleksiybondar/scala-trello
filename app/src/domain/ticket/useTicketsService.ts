import { useMutation, useQuery } from "@tanstack/react-query";

import { requestGraphQL } from "@helpers/requestGraphQL";
import { mapUiTicketStatusToStateKey } from "@helpers/uiTicketStatus";
import {
  buildChangeTicketAcceptanceCriteriaMutation,
  buildChangeTicketDescriptionMutation,
  buildChangeTicketEstimatedTimeMutation,
  buildChangeTicketStatusMutation,
  buildChangeTicketTitleMutation,
  buildCreateTicketMutation,
  buildReassignTicketMutation,
  buildTicketsQuery,
  mapTicketResponseToTicket
} from "./graphql";
import type { Ticket } from "./graphql";
import type {
  ChangeTicketAcceptanceCriteriaMutationResponse,
  ChangeTicketDescriptionMutationResponse,
  ChangeTicketEstimatedTimeMutationResponse,
  ChangeTicketStatusMutationResponse,
  ChangeTicketTitleMutationResponse,
  CreateTicketMutationResponse,
  ReassignTicketMutationResponse,
  TicketsQueryResponse
} from "./graphql";
import { useAuth } from "@hooks/useAuth";

export interface CreateBoardTicketInput {
  acceptanceCriteria: string;
  assignedToUserId: string | null;
  description: string;
  estimatedMinutes: number | null;
  title: string;
}

export type TicketsServiceStatus =
  | "new"
  | "in progress"
  | "code review"
  | "in testing"
  | "done";

export type TicketStatusKey =
    | "new"
    | "in_progress"
    | "code_review"
    | "in_testing"
    | "done";

export interface TicketsServiceSource {
  byId: Record<string, Ticket>;
  ids: string[];
}

export interface CreateTicketsServiceParams {
  boardId: string;
  ticketsRef: { current: TicketsServiceSource };
  updateVersioning: (states: TicketStatusKey[]) => void;
}

export interface UseTicketsService {
  createTicket: (input: CreateBoardTicketInput) => Promise<Ticket>;
  getAllTickets: () => Ticket[];
  isCreatingTicket: boolean;
  isReassigningTicket: boolean;
  isReloadingTickets: boolean;
  isTransitioningTicketState: boolean;
  reloadTickets: () => Promise<Ticket[]>;
  replaceAllTickets: (tickets: Ticket[]) => void;
  reassignTicket: (ticketId: string, assignedToUserId: string | null) => Promise<void>;
  ticketsError: Error | null;
  transitionTicketState: (ticketId: string, status: TicketsServiceStatus) => Promise<void>;
  updateTicket: (ticketId: string, updater: (ticket: Ticket) => Ticket) => Promise<void>;
}

const resolveTicketsServiceError = (...errors: (Error | null | undefined)[]): Error | null => {
  return errors.find((error): error is Error => error instanceof Error) ?? null;
};

const collectAffectedStates = (
  previousStatus: TicketsServiceStatus | null,
  nextStatus: TicketsServiceStatus | null
): TicketStatusKey[] => {
  const affectedStates = new Set<TicketStatusKey>();

  if (previousStatus !== null) {
    const previousKey = mapUiTicketStatusToStateKey(previousStatus);

    if (previousKey !== null) {
      affectedStates.add(previousKey);
    }
  }

  if (nextStatus !== null && nextStatus !== previousStatus) {
    const nextKey = mapUiTicketStatusToStateKey(nextStatus);

    if (nextKey !== null) {
      affectedStates.add(nextKey);
    }
  }

  return [...affectedStates];
};

const toSource = (tickets: Ticket[]): TicketsServiceSource => {
  return {
    byId: Object.fromEntries(tickets.map(ticket => [ticket.ticketId, ticket])),
    ids: tickets.map(ticket => ticket.ticketId)
  };
};

const updateTicketTitle = async (
  ticket: Ticket,
  currentTicket: Ticket,
  accessToken: string | null
): Promise<Ticket> => {
  if (ticket.name === currentTicket.name) {
    return currentTicket;
  }

  const response = await requestGraphQL<ChangeTicketTitleMutationResponse>({
    accessToken,
    document: buildChangeTicketTitleMutation(currentTicket.ticketId, ticket.name)
  });

  return mapTicketResponseToTicket(response.changeTicketTitle);
};

const updateTicketDescription = async (
  ticket: Ticket,
  currentTicket: Ticket,
  accessToken: string | null
): Promise<Ticket> => {
  if (ticket.description === currentTicket.description) {
    return currentTicket;
  }

  const response = await requestGraphQL<ChangeTicketDescriptionMutationResponse>({
    accessToken,
    document: buildChangeTicketDescriptionMutation(currentTicket.ticketId, ticket.description)
  });

  return mapTicketResponseToTicket(response.changeTicketDescription);
};

const updateTicketAcceptanceCriteria = async (
  ticket: Ticket,
  currentTicket: Ticket,
  accessToken: string | null
): Promise<Ticket> => {
  if (ticket.acceptanceCriteria === currentTicket.acceptanceCriteria) {
    return currentTicket;
  }

  const response = await requestGraphQL<ChangeTicketAcceptanceCriteriaMutationResponse>({
    accessToken,
    document: buildChangeTicketAcceptanceCriteriaMutation(
      currentTicket.ticketId,
      ticket.acceptanceCriteria
    )
  });

  return mapTicketResponseToTicket(response.changeTicketAcceptanceCriteria);
};

const updateTicketEstimatedTime = async (
  ticket: Ticket,
  currentTicket: Ticket,
  accessToken: string | null
): Promise<Ticket> => {
  if (ticket.estimatedMinutes === currentTicket.estimatedMinutes) {
    return currentTicket;
  }

  const response = await requestGraphQL<ChangeTicketEstimatedTimeMutationResponse>({
    accessToken,
    document: buildChangeTicketEstimatedTimeMutation(currentTicket.ticketId, ticket.estimatedMinutes)
  });

  return mapTicketResponseToTicket(response.changeTicketEstimatedTime);
};

const updateTicketAssignee = async (
  ticket: Ticket,
  currentTicket: Ticket,
  accessToken: string | null
): Promise<Ticket> => {
  if (ticket.assignedToUserId === currentTicket.assignedToUserId) {
    return currentTicket;
  }

  const response = await requestGraphQL<ReassignTicketMutationResponse>({
    accessToken,
    document: buildReassignTicketMutation(currentTicket.ticketId, ticket.assignedToUserId)
  });

  return mapTicketResponseToTicket(response.reassignTicket);
};

export const useTicketsService = ({
  boardId,
  ticketsRef,
  updateVersioning
}: CreateTicketsServiceParams): UseTicketsService => {
  const { accessToken } = useAuth();

  const getAllTickets = (): Ticket[] => {
    return ticketsRef.current.ids
      .map(ticketId => ticketsRef.current.byId[ticketId])
      .filter((ticket): ticket is Ticket => ticket !== undefined);
  };

  const replaceAllTickets = (tickets: Ticket[]): void => {
    ticketsRef.current = toSource(tickets);
  };

  const applyLocalTicketUpdate = (
    ticketId: string,
    updater: (ticket: Ticket) => Ticket
  ): void => {
    const currentTicket = ticketsRef.current.byId[ticketId];

    if (currentTicket === undefined) {
      return;
    }

    const previousStatus = currentTicket.status as TicketsServiceStatus | null;
    const nextTicket = updater(currentTicket);
    const nextStatus = nextTicket.status as TicketsServiceStatus | null;

    ticketsRef.current.byId[ticketId] = nextTicket;

    updateVersioning(collectAffectedStates(previousStatus, nextStatus));
  };

  const ticketsQuery = useQuery<Ticket[]>({
    enabled: false,
    queryFn: async (): Promise<Ticket[]> => {
      if (boardId.length === 0) {
        return getAllTickets();
      }

      const response = await requestGraphQL<TicketsQueryResponse>({
        accessToken,
        document: buildTicketsQuery(boardId)
      });
      const tickets = response.tickets.map(mapTicketResponseToTicket);

      replaceAllTickets(tickets);
      updateVersioning(["new", "in_progress", "code_review", "in_testing", "done"]);

      return tickets;
    },
    queryKey: ["tickets", boardId]
  });

  const createTicketMutation = useMutation<Ticket, Error, CreateBoardTicketInput>({
    mutationFn: async (input: CreateBoardTicketInput): Promise<Ticket> => {
      if (boardId.length === 0) {
        throw new Error("Board id is required to create a ticket.");
      }

      const response = await requestGraphQL<CreateTicketMutationResponse>({
        accessToken,
        document: buildCreateTicketMutation({
          acceptanceCriteria:
            input.acceptanceCriteria.trim().length > 0 ? input.acceptanceCriteria.trim() : null,
          assignedToUserId: input.assignedToUserId,
          boardId,
          description: input.description.trim().length > 0 ? input.description.trim() : null,
          estimatedMinutes: input.estimatedMinutes,
          title: input.title.trim()
        })
      });

      return mapTicketResponseToTicket(response.createTicket);
    },
    onSuccess: ticket => {
      ticketsRef.current.byId[ticket.ticketId] = ticket;
      ticketsRef.current.ids = [...ticketsRef.current.ids, ticket.ticketId];

      if (ticket.status !== null) {
        const stateKey = mapUiTicketStatusToStateKey(ticket.status);

        if (stateKey !== null) {
          updateVersioning([stateKey]);
        }
      }
    }
  });

  const reassignTicketMutation = useMutation<
    Ticket,
    Error,
    {
      assignedToUserId: string | null;
      ticketId: string;
    }
  >({
    mutationFn: async ({
      assignedToUserId,
      ticketId
    }: {
      assignedToUserId: string | null;
      ticketId: string;
    }): Promise<Ticket> => {
      const response = await requestGraphQL<ReassignTicketMutationResponse>({
        accessToken,
        document: buildReassignTicketMutation(ticketId, assignedToUserId)
      });

      return mapTicketResponseToTicket(response.reassignTicket);
    },
    onSuccess: (nextTicket, variables) => {
      applyLocalTicketUpdate(variables.ticketId, currentTicket => {
        return {
          ...currentTicket,
          ...nextTicket
        };
      });
    }
  });

  const updateTicketMutation = useMutation<
    Ticket,
    Error,
    {
      ticketId: string;
      updater: (ticket: Ticket) => Ticket;
    }
  >({
    mutationFn: async ({
      ticketId,
      updater
    }: {
      ticketId: string;
      updater: (ticket: Ticket) => Ticket;
    }): Promise<Ticket> => {
      const currentTicket = ticketsRef.current.byId[ticketId];

      if (currentTicket === undefined) {
        throw new Error(`Ticket ${ticketId} was not found.`);
      }

      const nextTicket = updater(currentTicket);

      if (nextTicket.status !== currentTicket.status) {
        throw new Error(
          "Changing ticket status is not supported by the current backend GraphQL schema."
        );
      }

      let latestTicket = currentTicket;

      latestTicket = await updateTicketTitle(nextTicket, currentTicket, accessToken);
      latestTicket = await updateTicketDescription(nextTicket, latestTicket, accessToken);
      latestTicket = await updateTicketAcceptanceCriteria(nextTicket, latestTicket, accessToken);
      latestTicket = await updateTicketEstimatedTime(nextTicket, latestTicket, accessToken);
      latestTicket = await updateTicketAssignee(nextTicket, latestTicket, accessToken);

      return latestTicket;
    },
    onSuccess: (nextTicket, variables) => {
      applyLocalTicketUpdate(variables.ticketId, () => nextTicket);
    }
  });

  const reloadTickets = async (): Promise<Ticket[]> => {
    const result = await ticketsQuery.refetch();

    if (result.data !== undefined) {
      return result.data;
    }

    return getAllTickets();
  };

  const createTicket: UseTicketsService["createTicket"] = async (
    input: CreateBoardTicketInput
  ): Promise<Ticket> => {
    return createTicketMutation.mutateAsync(input);
  };

  const reassignTicket: UseTicketsService["reassignTicket"] = async (
    ticketId: string,
    assignedToUserId: string | null
  ): Promise<void> => {
    await reassignTicketMutation.mutateAsync({
      assignedToUserId,
      ticketId
    });
  };

  const updateTicket: UseTicketsService["updateTicket"] = async (
    ticketId: string,
    updater: (ticket: Ticket) => Ticket
  ): Promise<void> => {
    await updateTicketMutation.mutateAsync({
      ticketId,
      updater
    });
  };

  const transitionTicketStateMutation = useMutation<
    Ticket,
    Error,
    {
      status: TicketsServiceStatus;
      ticketId: string;
    }
  >({
    mutationFn: async ({
      status,
      ticketId
    }: {
      status: TicketsServiceStatus;
      ticketId: string;
    }): Promise<Ticket> => {
      const response = await requestGraphQL<ChangeTicketStatusMutationResponse>({
        accessToken,
        document: buildChangeTicketStatusMutation(ticketId, status)
      });

      return mapTicketResponseToTicket(response.changeTicketStatus);
    },
    onSuccess: (nextTicket, variables) => {
      applyLocalTicketUpdate(variables.ticketId, () => nextTicket);
    }
  });

  const transitionTicketState: UseTicketsService["transitionTicketState"] = async (
    ticketId: string,
    status: TicketsServiceStatus
  ): Promise<void> => {
    await transitionTicketStateMutation.mutateAsync({
      status,
      ticketId
    });
  };

  return {
    createTicket,
    getAllTickets,
    isCreatingTicket: createTicketMutation.isPending,
    isReassigningTicket: reassignTicketMutation.isPending,
    isReloadingTickets: ticketsQuery.isFetching,
    isTransitioningTicketState: transitionTicketStateMutation.isPending,
    reassignTicket,
    reloadTickets,
    replaceAllTickets,
    ticketsError: resolveTicketsServiceError(
      ticketsQuery.error,
      createTicketMutation.error,
      reassignTicketMutation.error,
      transitionTicketStateMutation.error,
      updateTicketMutation.error
    ),
    transitionTicketState,
    updateTicket
  };
};

export const createTicketsService = useTicketsService;
