import { useContext } from "react";

import { TicketContext } from "@contexts/ticket-context";
import type { TicketContextValue } from "@contexts/ticket-context";

export const useTicket = (): TicketContextValue => {
  return useContext(TicketContext);
};
