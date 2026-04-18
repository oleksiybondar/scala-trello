import { createContext } from "react";

import type { TimeTrackingEntry } from "../domain/time-tracking/graphql";

export type TimeTrackingBoardStateFilter = "active" | "all" | "inactive";

export interface QueryTimeTrackingEntriesParams {
  activityIds?: string[] | undefined;
  boardIds?: string[] | undefined;
  boardState?: TimeTrackingBoardStateFilter | undefined;
  keyword?: string | undefined;
  page?: number | undefined;
}

export interface NormalizedQueryTimeTrackingEntriesParams {
  activityIds: string[];
  boardIds: string[];
  boardState: TimeTrackingBoardStateFilter;
  keyword?: string | undefined;
  page: number;
}

export interface TimeTrackingEntriesContextValue {
  currentParams: NormalizedQueryTimeTrackingEntriesParams;
  isLoadingEntries: boolean;
  queryEntries: (params: QueryTimeTrackingEntriesParams) => void;
  timeTrackingEntries: TimeTrackingEntry[];
  timeTrackingEntriesError: Error | null;
  totalTimeTrackingEntries: number;
}

const missingTimeTrackingEntriesProvider = (): never => {
  throw new Error("TimeTrackingEntriesContext is missing its provider.");
};

export const TimeTrackingEntriesContext = createContext<TimeTrackingEntriesContextValue>({
  currentParams: {
    activityIds: [],
    boardIds: [],
    boardState: "all",
    page: 1
  },
  isLoadingEntries: false,
  queryEntries: missingTimeTrackingEntriesProvider,
  timeTrackingEntries: [],
  timeTrackingEntriesError: null,
  totalTimeTrackingEntries: 0
});
