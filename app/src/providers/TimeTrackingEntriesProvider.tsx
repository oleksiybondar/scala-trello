import type { PropsWithChildren, ReactElement } from "react";
import { useState } from "react";

import { TimeTrackingEntriesContext } from "@contexts/time-tracking-entries-context";
import type {
  NormalizedQueryTimeTrackingEntriesParams,
  QueryTimeTrackingEntriesParams
} from "@contexts/time-tracking-entries-context";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { useTimeTrackingEntriesService } from "../domain/time-tracking/useTimeTrackingEntriesService";

const DEFAULT_QUERY_TIME_TRACKING_ENTRIES_PARAMS: NormalizedQueryTimeTrackingEntriesParams = {
  activityIds: [],
  boardIds: [],
  boardState: "all",
  page: 1
};

const normalizeQueryTimeTrackingEntriesParams = (
  params: QueryTimeTrackingEntriesParams
): NormalizedQueryTimeTrackingEntriesParams => {
  return {
    activityIds: params.activityIds ?? DEFAULT_QUERY_TIME_TRACKING_ENTRIES_PARAMS.activityIds,
    boardIds: params.boardIds ?? DEFAULT_QUERY_TIME_TRACKING_ENTRIES_PARAMS.boardIds,
    boardState: params.boardState ?? DEFAULT_QUERY_TIME_TRACKING_ENTRIES_PARAMS.boardState,
    keyword: params.keyword,
    page: params.page ?? DEFAULT_QUERY_TIME_TRACKING_ENTRIES_PARAMS.page
  };
};

export const TimeTrackingEntriesProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const { userId } = useCurrentUser();
  const [currentParams, setCurrentParams] = useState(
    normalizeQueryTimeTrackingEntriesParams(DEFAULT_QUERY_TIME_TRACKING_ENTRIES_PARAMS)
  );
  const {
    isLoadingEntries,
    timeTrackingEntries,
    timeTrackingEntriesError,
    totalTimeTrackingEntries
  } = useTimeTrackingEntriesService({
    currentParams,
    userId
  });

  return (
    <TimeTrackingEntriesContext.Provider
      value={{
        currentParams,
        isLoadingEntries,
        queryEntries: (params: QueryTimeTrackingEntriesParams) => {
          setCurrentParams(currentState => {
            return normalizeQueryTimeTrackingEntriesParams({
              ...currentState,
              ...params
            });
          });
        },
        timeTrackingEntries,
        timeTrackingEntriesError,
        totalTimeTrackingEntries
      }}
    >
      {children}
    </TimeTrackingEntriesContext.Provider>
  );
};
