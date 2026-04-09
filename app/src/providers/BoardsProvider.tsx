import type { PropsWithChildren, ReactElement } from "react";
import { useState } from "react";

import { BoardsContext } from "@contexts/boards-context";
import type {
  NormalizedQueryBoardsParams,
  QueryBoardsParams
} from "@contexts/boards-context";
import { filterBoards, getBoardOwnerOptions } from "@features/board/boardsApi";
import { useCreateBoardMutation } from "@features/board/useCreateBoardMutation";
import { useBoardsQuery } from "@features/board/useBoardsQuery";
import type { CreateBoardInput } from "@models/board";

const DEFAULT_QUERY_BOARDS_PARAMS = {
  active: true as const,
  page: 1
} as const;

const normalizeQueryBoardsParams = (
  params: QueryBoardsParams
): NormalizedQueryBoardsParams => {
  return {
    active: params.active ?? DEFAULT_QUERY_BOARDS_PARAMS.active,
    keyword: params.keyword,
    owner: params.owner,
    page: params.page ?? DEFAULT_QUERY_BOARDS_PARAMS.page
  };
};

/**
 * Thin facade over board-related query and mutation hooks.
 */
export const BoardsProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const [currentParams, setCurrentParams] = useState(
    normalizeQueryBoardsParams(DEFAULT_QUERY_BOARDS_PARAMS)
  );
  const boardsQuery = useBoardsQuery({
    keyword: currentParams.keyword,
    owner: currentParams.owner
  });
  const ownerOptionsQuery = useBoardsQuery({});
  const createBoardMutation = useCreateBoardMutation();
  const visibleBoards = boardsQuery.data ?? [];
  const allBoards = ownerOptionsQuery.data ?? [];

  const value = {
    boards: filterBoards(visibleBoards, {
      active: currentParams.active,
      page: currentParams.page
    }),
    boardsError:
      boardsQuery.error instanceof Error ? boardsQuery.error : null,
    createBoard: async (input: CreateBoardInput) => {
      await createBoardMutation.mutateAsync(input);
    },
    currentParams,
    isCreatingBoard: createBoardMutation.isPending,
    isLoadingBoards: boardsQuery.isLoading || ownerOptionsQuery.isLoading,
    ownerOptions: getBoardOwnerOptions(allBoards),
    queryBoards: (params: QueryBoardsParams) => {
      setCurrentParams(currentState => {
        return normalizeQueryBoardsParams({
          ...currentState,
          ...params
        });
      });
    }
  };

  return <BoardsContext.Provider value={value}>{children}</BoardsContext.Provider>;
};
