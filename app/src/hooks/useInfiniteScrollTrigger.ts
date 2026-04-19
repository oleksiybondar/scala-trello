import { useEffect, useRef } from "react";
import type { RefObject } from "react";

interface UseInfiniteScrollTriggerParams {
  canLoadMore: boolean;
  isLoadingMore: boolean;
  onLoadMore: () => Promise<void>;
}

export const useInfiniteScrollTrigger = ({
  canLoadMore,
  isLoadingMore,
  onLoadMore
}: UseInfiniteScrollTriggerParams): RefObject<HTMLDivElement | null> => {
  const sentinelRef = useRef<HTMLDivElement>(null);
  const canLoadMoreRef = useRef(canLoadMore);
  const isLoadingMoreRef = useRef(isLoadingMore);
  const onLoadMoreRef = useRef(onLoadMore);

  useEffect(() => {
    canLoadMoreRef.current = canLoadMore;
  }, [canLoadMore]);

  useEffect(() => {
    isLoadingMoreRef.current = isLoadingMore;
  }, [isLoadingMore]);

  useEffect(() => {
    onLoadMoreRef.current = onLoadMore;
  }, [onLoadMore]);

  useEffect(() => {
    const sentinel = sentinelRef.current;

    if (sentinel === null || typeof IntersectionObserver === "undefined") {
      return;
    }

    const observer = new IntersectionObserver(
      entries => {
        if (!entries.some(entry => entry.isIntersecting)) {
          return;
        }

        if (!canLoadMoreRef.current || isLoadingMoreRef.current) {
          return;
        }

        void onLoadMoreRef.current();
      },
      {
        rootMargin: "240px 0px"
      }
    );

    observer.observe(sentinel);

    return () => {
      observer.disconnect();
    };
  }, []);

  return sentinelRef;
};
