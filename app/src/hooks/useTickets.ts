import { useContext } from "react";

import { TicketsContext } from "@contexts/tickets-context";
import type { TicketsContextValue } from "@contexts/tickets-context";

export const useTickets = (): TicketsContextValue => {
  return useContext(TicketsContext);
};
