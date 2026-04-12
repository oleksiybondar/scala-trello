import type { PropsWithChildren, ReactElement } from "react";
import { useMemo } from "react";

import { TicketsContext } from "@contexts/tickets-context";
import { useBoard } from "@hooks/useBoard";

export const TicketsProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const { board } = useBoard();
  const tickets = board?.tickets ?? [];
  const ticketsCount = useMemo(() => {
    return tickets.length;
  }, [tickets]);
  const value = useMemo(() => {
    return {
      tickets,
      ticketsCount
    };
  }, [tickets, ticketsCount]);

  return <TicketsContext.Provider value={value}>{children}</TicketsContext.Provider>;
};
