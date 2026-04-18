import { useMemo } from "react";

import { useQuery } from "@tanstack/react-query";

import type { NormalizedQueryTimeTrackingEntriesParams } from "@contexts/time-tracking-entries-context";
import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildTimeTrackingEntriesByUserQuery,
  mapTimeTrackingEntryResponseToTimeTrackingEntry
} from "./graphql";
import type {
  TimeTrackingEntriesByUserQueryResponse,
  TimeTrackingEntry
} from "./graphql";
import {
  countTimeTrackingEntries,
  filterTimeTrackingEntries
} from "./timeTrackingEntriesApi";

interface UseTimeTrackingEntriesServiceParams {
  currentParams: NormalizedQueryTimeTrackingEntriesParams;
  userId: string | null;
}

interface UseTimeTrackingEntriesServiceResult {
  isLoadingEntries: boolean;
  timeTrackingEntries: TimeTrackingEntry[];
  timeTrackingEntriesError: Error | null;
  totalTimeTrackingEntries: number;
}

export const useTimeTrackingEntriesService = ({
  currentParams,
  userId
}: UseTimeTrackingEntriesServiceParams): UseTimeTrackingEntriesServiceResult => {
  const { accessToken, session } = useAuth();
  const isEnabled = accessToken !== null && session !== null && userId !== null;

  const timeTrackingEntriesQuery = useQuery({
    enabled: isEnabled,
    queryFn: async (): Promise<TimeTrackingEntry[]> => {
      if (accessToken === null || session === null || userId === null) {
        throw new Error("Authentication context is required for time-tracking operations.");
      }

      const response = await requestGraphQL<TimeTrackingEntriesByUserQueryResponse>({
        accessToken,
        document: buildTimeTrackingEntriesByUserQuery(userId),
        tokenType: session.tokenType
      });

      return response.timeTrackingEntriesByUser.map(mapTimeTrackingEntryResponseToTimeTrackingEntry);
    },
    queryKey: [
      "my-time-tracking-entries",
      userId ?? "anonymous",
      currentParams.keyword ?? "",
      currentParams.page,
      currentParams.boardState,
      currentParams.activityIds.join(","),
      currentParams.boardIds.join(",")
    ]
  });

  const timeTrackingEntries = useMemo(() => {
    return filterTimeTrackingEntries(timeTrackingEntriesQuery.data ?? [], currentParams);
  }, [currentParams, timeTrackingEntriesQuery.data]);
  const totalTimeTrackingEntries = useMemo(() => {
    return countTimeTrackingEntries(timeTrackingEntriesQuery.data ?? [], currentParams);
  }, [currentParams, timeTrackingEntriesQuery.data]);

  return {
    isLoadingEntries: timeTrackingEntriesQuery.isLoading,
    timeTrackingEntries,
    timeTrackingEntriesError:
      timeTrackingEntriesQuery.error instanceof Error ? timeTrackingEntriesQuery.error : null,
    totalTimeTrackingEntries
  };
};
