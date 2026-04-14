import { createContext } from "react";

export interface TimeTrackingContextValue {
  closeLogTimeModal: () => void;
  isLogTimeModalOpen: boolean;
  openLogTimeModal: (ticketId: string) => void;
  selectedTicketId: string | null;
}

const missingTimeTrackingProvider = (): never => {
  throw new Error("TimeTrackingContext is missing its provider.");
};

export const TimeTrackingContext = createContext<TimeTrackingContextValue>({
  closeLogTimeModal: missingTimeTrackingProvider,
  isLogTimeModalOpen: false,
  openLogTimeModal: missingTimeTrackingProvider,
  selectedTicketId: null
});
