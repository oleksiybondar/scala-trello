import { useCallback, useEffect, useMemo, useRef } from "react";

import { useInfiniteQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import type { InfiniteData } from "@tanstack/react-query";

import type { NormalizedQueryBoardsParams } from "@contexts/boards-context";
import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildCreateBoardMutation,
  buildMyBoardsQuery,
  mapBoardResponseToBoard,
  mapCreateBoardInputToRequest
} from "./graphql";
import type {
  Board,
  CreateBoardInput,
  CreateBoardMutationResponse,
  MyBoardsQueryResponse
} from "./graphql";

interface BoardsService {
  boards: Board[];
  canLoadMoreBoards: boolean;
  boardsError: Error | null;
  createBoard: (input: CreateBoardInput) => Promise<Board>;
  loadNextBoardsPage: () => Promise<void>;
  isCreatingBoard: boolean;
  isLoadingBoards: boolean;
  isLoadingMoreBoards: boolean;
}

interface UseBoardsServiceParams {
  currentParams: NormalizedQueryBoardsParams;
}

export const useBoardsService = ({
  currentParams
}: UseBoardsServiceParams): BoardsService => {
  const BOARDS_PER_PAGE = 10;
  const { accessToken, session } = useAuth();
  const queryClient = useQueryClient();
  const isEnabled = accessToken !== null && session !== null;
  const isLoadingMoreRef = useRef(false);

  const boardsQuery = useInfiniteQuery<
    Board[],
    Error,
    InfiniteData<Board[]>,
    readonly unknown[],
    number
  >({
    enabled: isEnabled,
    getNextPageParam: (lastPage, allPages) => {
      if (lastPage.length < BOARDS_PER_PAGE) {
        return undefined;
      }

      return allPages.reduce((total, page) => total + page.length, 0);
    },
    initialPageParam: 0,
    queryFn: async ({ pageParam }) => {
      if (accessToken === null || session === null) {
        throw new Error("Authentication context is required for board operations.");
      }

      const response = await requestGraphQL<MyBoardsQueryResponse>({
        accessToken,
        document: buildMyBoardsQuery({
          active: currentParams.showInactive ? undefined : true,
          keyword: currentParams.keyword,
          limit: BOARDS_PER_PAGE,
          offset: pageParam,
          ownerUserId: currentParams.owner
        }),
        tokenType: session.tokenType
      });

      return response.myBoards.map(mapBoardResponseToBoard);
    },
    queryKey: [
      "boards",
      currentParams.showInactive ? "all" : "active",
      currentParams.keyword ?? "",
      currentParams.owner ?? ""
    ]
  });

  const createBoardMutation = useMutation({
    mutationFn: async (input: CreateBoardInput) => {
      const response = await requestGraphQL<CreateBoardMutationResponse>({
        accessToken,
        document: buildCreateBoardMutation(mapCreateBoardInputToRequest(input)),
      });

      return mapBoardResponseToBoard(response.createBoard);
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["boards"]
        }),
        queryClient.invalidateQueries({
          queryKey: ["board"]
        })
      ]);
    }
  });

  useEffect(() => {
    isLoadingMoreRef.current = boardsQuery.isFetchingNextPage;
  }, [boardsQuery.isFetchingNextPage]);

  const boards = useMemo(() => {
    return boardsQuery.data?.pages.flatMap(page => page) ?? [];
  }, [boardsQuery.data?.pages]);

  const loadNextBoardsPage = useCallback(async () => {
    if (isLoadingMoreRef.current || !boardsQuery.hasNextPage) {
      return;
    }

    isLoadingMoreRef.current = true;

    try {
      await boardsQuery.fetchNextPage();
    } finally {
      isLoadingMoreRef.current = false;
    }
  }, [boardsQuery.fetchNextPage, boardsQuery.hasNextPage]);

  return {
    boards,
    canLoadMoreBoards: boardsQuery.hasNextPage,
    boardsError: boardsQuery.error instanceof Error ? boardsQuery.error : null,
    createBoard: async (input: CreateBoardInput) => {
      return createBoardMutation.mutateAsync(input);
    },
    loadNextBoardsPage,
    isCreatingBoard: createBoardMutation.isPending,
    isLoadingBoards: boardsQuery.isLoading,
    isLoadingMoreBoards: boardsQuery.isFetchingNextPage
  };
};
