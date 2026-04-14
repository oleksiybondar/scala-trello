import { createContext } from "react";

import type { Ticket } from "../domain/ticket/graphql";

export interface QueryMyTicketsParams {
  assignedOnly?: boolean | undefined;
  keyword?: string | undefined;
  page?: number | undefined;
  priorities?: number[] | undefined;
  severityIds?: string[] | undefined;
}

export interface NormalizedQueryMyTicketsParams {
  assignedOnly: boolean;
  keyword?: string | undefined;
  page: number;
  priorities: number[];
  severityIds: string[];
}

export interface MyTicketsContextValue {
  currentParams: NormalizedQueryMyTicketsParams;
  isLoadingMyTickets: boolean;
  myTickets: Ticket[];
  myTicketsError: Error | null;
  queryMyTickets: (params: QueryMyTicketsParams) => void;
  totalMyTickets: number;
}

const missingMyTicketsProvider = (): never => {
  throw new Error("MyTicketsContext is missing its provider.");
};

export const MyTicketsContext = createContext<MyTicketsContextValue>({
  currentParams: {
    assignedOnly: false,
    page: 1,
    priorities: [],
    severityIds: []
  },
  isLoadingMyTickets: false,
  myTickets: [],
  myTicketsError: null,
  queryMyTickets: missingMyTicketsProvider,
  totalMyTickets: 0
});
