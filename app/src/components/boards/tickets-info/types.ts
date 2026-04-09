export interface TicketStateCounts {
  code_review: number;
  done: number;
  in_progress: number;
  in_testing: number;
  new: number;
}

export type TicketStateKey = keyof TicketStateCounts;
