import type { Dispatch, SetStateAction } from "react";
import { useState } from "react";

import type { Ticket } from "../../domain/ticket/graphql";
import type { TicketStatusKey } from "../../domain/ticket/useTicketsService";

export interface UseTicketsStateResult {
  codeReviewTickets: Ticket[];
  codeReviewTicketsRevision: number;
  doneTickets: Ticket[];
  doneTicketsRevision: number;
  inProgressTickets: Ticket[];
  inProgressTicketsRevision: number;
  inTestingTickets: Ticket[];
  inTestingTicketsRevision: number;
  newTickets: Ticket[];
  newTicketsRevision: number;
  setCodeReviewTickets: Dispatch<SetStateAction<Ticket[]>>;
  setDoneTickets: Dispatch<SetStateAction<Ticket[]>>;
  setInProgressTickets: Dispatch<SetStateAction<Ticket[]>>;
  setInTestingTickets: Dispatch<SetStateAction<Ticket[]>>;
  setNewTickets: Dispatch<SetStateAction<Ticket[]>>;
  updateVersioning: (states: TicketStatusKey[]) => void;
}

export const useTicketsState = (): UseTicketsStateResult => {
  const [newTickets, setNewTickets] = useState<Ticket[]>([]);
  const [inProgressTickets, setInProgressTickets] = useState<Ticket[]>([]);
  const [codeReviewTickets, setCodeReviewTickets] = useState<Ticket[]>([]);
  const [inTestingTickets, setInTestingTickets] = useState<Ticket[]>([]);
  const [doneTickets, setDoneTickets] = useState<Ticket[]>([]);
  const [newTicketsRevision, setNewTicketsRevision] = useState(0);
  const [inProgressTicketsRevision, setInProgressTicketsRevision] = useState(0);
  const [codeReviewTicketsRevision, setCodeReviewTicketsRevision] = useState(0);
  const [inTestingTicketsRevision, setInTestingTicketsRevision] = useState(0);
  const [doneTicketsRevision, setDoneTicketsRevision] = useState(0);

  const updateVersioning = (states: TicketStatusKey[]): void => {
    const affectedStates = new Set(states);

    if (affectedStates.has("new")) {
      setNewTicketsRevision(version => version + 1);
    }

    if (affectedStates.has("in_progress")) {
      setInProgressTicketsRevision(version => version + 1);
    }

    if (affectedStates.has("code_review")) {
      setCodeReviewTicketsRevision(version => version + 1);
    }

    if (affectedStates.has("in_testing")) {
      setInTestingTicketsRevision(version => version + 1);
    }

    if (affectedStates.has("done")) {
      setDoneTicketsRevision(version => version + 1);
    }
  };

  return {
    codeReviewTickets,
    codeReviewTicketsRevision,
    doneTickets,
    doneTicketsRevision,
    inProgressTickets,
    inProgressTicketsRevision,
    inTestingTickets,
    inTestingTicketsRevision,
    newTickets,
    newTicketsRevision,
    setCodeReviewTickets,
    setDoneTickets,
    setInProgressTickets,
    setInTestingTickets,
    setNewTickets,
    updateVersioning
  };
};
