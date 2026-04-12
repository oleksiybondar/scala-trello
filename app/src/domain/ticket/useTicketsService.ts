import { useMutation, useQuery } from "@tanstack/react-query";

import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildCreateTicketMutation,
  buildReassignTicketMutation,
  buildTicketsQuery,
  mapTicketResponseToTicket
} from "./graphql";
import type { Ticket } from "./graphql";
import type {
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

export interface TicketsServiceSource {
  byId: Record<string, Ticket>;
  ids: string[];
}

export interface CreateTicketsServiceParams {
  boardId: string;
  ticketsRef: { current: TicketsServiceSource };
  updateVersioning: (states: TicketsServiceStatus[]) => void;
}

export interface UseTicketsService {
  createTicket: (input: CreateBoardTicketInput) => Promise<Ticket>;
  getAllTickets: () => Ticket[];
  isCreatingTicket: boolean;
  isReassigningTicket: boolean;
  isReloadingTickets: boolean;
  reloadTickets: () => Promise<Ticket[]>;
  replaceAllTickets: (tickets: Ticket[]) => void;
  reassignTicket: (ticketId: string, assignedToUserId: string | null) => Promise<void>;
  ticketsError: Error | null;
  transitionTicketState: (ticketId: string, status: TicketsServiceStatus) => void;
  updateTicket: (ticketId: string, updater: (ticket: Ticket) => Ticket) => void;
}

const resolveTicketsServiceError = (...errors: (Error | null | undefined)[]): Error | null => {
  return errors.find((error): error is Error => error instanceof Error) ?? null;
};

const collectAffectedStates = (
  previousStatus: TicketsServiceStatus | null,
  nextStatus: TicketsServiceStatus | null
): TicketsServiceStatus[] => {
  const affectedStates: TicketsServiceStatus[] = [];

  if (previousStatus !== null) {
    affectedStates.push(previousStatus);
  }

  if (nextStatus !== null && nextStatus !== previousStatus) {
    affectedStates.push(nextStatus);
  }

  return affectedStates;
};

const toSource = (tickets: Ticket[]): TicketsServiceSource => {
  return {
    byId: Object.fromEntries(tickets.map(ticket => [ticket.ticketId, ticket])),
    ids: tickets.map(ticket => ticket.ticketId)
  };
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

  const updateTicket = (
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
      updateVersioning(["new", "in progress", "code review", "in testing", "done"]);

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
        updateVersioning([ticket.status as TicketsServiceStatus]);
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
      updateTicket(variables.ticketId, currentTicket => {
        return {
          ...currentTicket,
          ...nextTicket
        };
      });
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

  const transitionTicketState = (
    ticketId: string,
    status: TicketsServiceStatus
  ): void => {
    updateTicket(ticketId, ticket => {
      return {
        ...ticket,
        status
      };
    });
  };

  return {
    createTicket,
    getAllTickets,
    isCreatingTicket: createTicketMutation.isPending,
    isReassigningTicket: reassignTicketMutation.isPending,
    isReloadingTickets: ticketsQuery.isFetching,
    reassignTicket,
    reloadTickets,
    replaceAllTickets,
    ticketsError: resolveTicketsServiceError(
      ticketsQuery.error,
      createTicketMutation.error,
      reassignTicketMutation.error
    ),
    transitionTicketState,
    updateTicket
  };
};

export const createTicketsService = useTicketsService;
