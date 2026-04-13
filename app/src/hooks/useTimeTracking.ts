import { useContext } from "react";

import { TimeTrackingContext } from "@contexts/time-tracking-context";
import type { TimeTrackingContextValue } from "@contexts/time-tracking-context";

export const useTimeTracking = (): TimeTrackingContextValue => {
  return useContext(TimeTrackingContext);
};
