import { createContext } from "react";

import type { BoardTicket } from "@models/board";

export interface TicketsContextValue {
  tickets: BoardTicket[];
  ticketsCount: number;
}

export const TicketsContext = createContext<TicketsContextValue>({
  tickets: [],
  ticketsCount: 0
});
