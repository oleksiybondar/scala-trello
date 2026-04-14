import type { PropsWithChildren, ReactElement } from "react";
import { useState } from "react";

import { MyTicketsContext } from "@contexts/my-tickets-context";
import type {
  NormalizedQueryMyTicketsParams,
  QueryMyTicketsParams
} from "@contexts/my-tickets-context";
import { useMyTicketsService } from "../domain/ticket/useMyTicketsService";

const DEFAULT_QUERY_MY_TICKETS_PARAMS: NormalizedQueryMyTicketsParams = {
  assignedOnly: false,
  page: 1,
  priorities: [],
  severityIds: []
};

const normalizeQueryMyTicketsParams = (
  params: QueryMyTicketsParams
): NormalizedQueryMyTicketsParams => {
  return {
    assignedOnly: params.assignedOnly ?? DEFAULT_QUERY_MY_TICKETS_PARAMS.assignedOnly,
    keyword: params.keyword,
    page: params.page ?? DEFAULT_QUERY_MY_TICKETS_PARAMS.page,
    priorities: params.priorities ?? DEFAULT_QUERY_MY_TICKETS_PARAMS.priorities,
    severityIds: params.severityIds ?? DEFAULT_QUERY_MY_TICKETS_PARAMS.severityIds
  };
};

export const MyTicketsProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const [currentParams, setCurrentParams] = useState(
    normalizeQueryMyTicketsParams(DEFAULT_QUERY_MY_TICKETS_PARAMS)
  );
  const { isLoadingMyTickets, myTickets, myTicketsError, totalMyTickets } =
    useMyTicketsService({
      currentParams
    });

  return (
    <MyTicketsContext.Provider
      value={{
        currentParams,
        isLoadingMyTickets,
        myTickets,
        myTicketsError,
        queryMyTickets: (params: QueryMyTicketsParams) => {
          setCurrentParams(currentState => {
            return normalizeQueryMyTicketsParams({
              ...currentState,
              ...params
            });
          });
        },
        totalMyTickets
      }}
    >
      {children}
    </MyTicketsContext.Provider>
  );
};
