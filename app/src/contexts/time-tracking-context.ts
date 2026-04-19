import { createContext } from "react";

import type { TimeTrackingEntry } from "../domain/time-tracking/graphql";
import type { RegisterTimeTrackingEntryInput } from "../domain/time-tracking/useTimeTrackingEntryService";

export type TimeTrackingOnRegister = (
  entry: TimeTrackingEntry
) => void | Promise<void>;

export interface RegisterTimeTrackingOptions {
  onRegister?: TimeTrackingOnRegister;
}

export interface TimeTrackingContextValue {
  closeLogTimeModal: () => void;
  isLogTimeModalOpen: boolean;
  isRegisteringTime: boolean;
  openLogTimeModal: (ticketId: string) => void;
  registerTime: (
    input: RegisterTimeTrackingEntryInput,
    options?: RegisterTimeTrackingOptions
  ) => Promise<TimeTrackingEntry>;
  selectedTicketId: string | null;
  setOnRegister: (onRegister?: TimeTrackingOnRegister) => void;
}

const missingTimeTrackingProvider = (): never => {
  throw new Error("TimeTrackingContext is missing its provider.");
};

export const TimeTrackingContext = createContext<TimeTrackingContextValue>({
  closeLogTimeModal: missingTimeTrackingProvider,
  isLogTimeModalOpen: false,
  isRegisteringTime: false,
  openLogTimeModal: missingTimeTrackingProvider,
  registerTime: missingTimeTrackingProvider,
  selectedTicketId: null,
  setOnRegister: missingTimeTrackingProvider
});
