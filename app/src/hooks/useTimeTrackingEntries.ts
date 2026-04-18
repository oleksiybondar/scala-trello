import { useContext } from "react";

import { TimeTrackingEntriesContext } from "@contexts/time-tracking-entries-context";
import type { TimeTrackingEntriesContextValue } from "@contexts/time-tracking-entries-context";

export const useTimeTrackingEntries = (): TimeTrackingEntriesContextValue => {
  return useContext(TimeTrackingEntriesContext);
};
