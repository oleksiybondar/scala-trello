import { useContext } from "react";

import { MyTicketsContext } from "@contexts/my-tickets-context";
import type { MyTicketsContextValue } from "@contexts/my-tickets-context";

export const useMyTickets = (): MyTicketsContextValue => {
  return useContext(MyTicketsContext);
};
