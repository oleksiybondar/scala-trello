import { useCallback, useEffect, useMemo, useRef } from "react";

import { useInfiniteQuery } from "@tanstack/react-query";
import type { InfiniteData } from "@tanstack/react-query";

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
  canLoadMoreEntries: boolean;
  isLoadingEntries: boolean;
  isLoadingNextEntriesPage: boolean;
  loadNextEntriesPage: () => Promise<void>;
  timeTrackingEntries: TimeTrackingEntry[];
  timeTrackingEntriesError: Error | null;
  totalTimeTrackingEntries: number;
}

export const useTimeTrackingEntriesService = ({
  currentParams,
  userId
}: UseTimeTrackingEntriesServiceParams): UseTimeTrackingEntriesServiceResult => {
  const TIME_TRACKING_ENTRIES_PER_PAGE = 50;
  const { accessToken, session } = useAuth();
  const isEnabled = accessToken !== null && session !== null && userId !== null;
  const isLoadingMoreRef = useRef(false);

  const timeTrackingEntriesQuery = useInfiniteQuery<
    TimeTrackingEntry[],
    Error,
    InfiniteData<TimeTrackingEntry[]>,
    readonly unknown[],
    number
  >({
    enabled: isEnabled,
    getNextPageParam: (lastPage, allPages) => {
      if (lastPage.length < TIME_TRACKING_ENTRIES_PER_PAGE) {
        return undefined;
      }

      return allPages.reduce((total, page) => total + page.length, 0);
    },
    initialPageParam: 0,
    queryFn: async ({ pageParam }): Promise<TimeTrackingEntry[]> => {
      if (accessToken === null || session === null || userId === null) {
        throw new Error("Authentication context is required for time-tracking operations.");
      }

      const response = await requestGraphQL<TimeTrackingEntriesByUserQueryResponse>({
        accessToken,
        document: buildTimeTrackingEntriesByUserQuery(
          userId,
          pageParam,
          TIME_TRACKING_ENTRIES_PER_PAGE
        ),
        tokenType: session.tokenType
      });

      return response.timeTrackingEntriesByUser.map(mapTimeTrackingEntryResponseToTimeTrackingEntry);
    },
    queryKey: [
      "my-time-tracking-entries",
      userId ?? "anonymous",
      currentParams.keyword ?? "",
      currentParams.boardState,
      currentParams.activityIds.join(","),
      currentParams.boardIds.join(",")
    ]
  });

  const allEntries = useMemo(() => {
    return timeTrackingEntriesQuery.data?.pages.flatMap(page => page) ?? [];
  }, [timeTrackingEntriesQuery.data?.pages]);
  const timeTrackingEntries = useMemo(() => {
    return filterTimeTrackingEntries(allEntries, currentParams);
  }, [allEntries, currentParams]);
  const totalTimeTrackingEntries = useMemo(() => {
    return countTimeTrackingEntries(allEntries, currentParams);
  }, [allEntries, currentParams]);

  useEffect(() => {
    isLoadingMoreRef.current = timeTrackingEntriesQuery.isFetchingNextPage;
  }, [timeTrackingEntriesQuery.isFetchingNextPage]);

  const loadNextEntriesPage = useCallback(async () => {
    if (isLoadingMoreRef.current || !timeTrackingEntriesQuery.hasNextPage) {
      return;
    }

    isLoadingMoreRef.current = true;

    try {
      await timeTrackingEntriesQuery.fetchNextPage();
    } finally {
      isLoadingMoreRef.current = false;
    }
  }, [timeTrackingEntriesQuery.fetchNextPage, timeTrackingEntriesQuery.hasNextPage]);

  return {
    canLoadMoreEntries: timeTrackingEntriesQuery.hasNextPage,
    isLoadingEntries: timeTrackingEntriesQuery.isLoading,
    isLoadingNextEntriesPage: timeTrackingEntriesQuery.isFetchingNextPage,
    loadNextEntriesPage,
    timeTrackingEntries,
    timeTrackingEntriesError:
      timeTrackingEntriesQuery.error instanceof Error ? timeTrackingEntriesQuery.error : null,
    totalTimeTrackingEntries
  };
};
