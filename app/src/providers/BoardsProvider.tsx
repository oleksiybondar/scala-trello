import type { PropsWithChildren, ReactElement } from "react";
import { useMemo, useState } from "react";

import { BoardsContext } from "@contexts/boards-context";
import type {
  NormalizedQueryBoardsParams,
  QueryBoardsParams
} from "@contexts/boards-context";
import { useCreateBoardMutation } from "@features/board/useCreateBoardMutation";
import { useBoardsQuery } from "@features/board/useBoardsQuery";
import { useCurrentUser } from "@hooks/useCurrentUser";
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
  const { userId } = useCurrentUser();
  const [currentParams, setCurrentParams] = useState(
    normalizeQueryBoardsParams(DEFAULT_QUERY_BOARDS_PARAMS)
  );
  const boardsQuery = useBoardsQuery(currentParams);
  const createBoardMutation = useCreateBoardMutation(userId ?? "anonymous-user");

  const value = useMemo(
    () => ({
      boards: boardsQuery.data ?? [],
      boardsError:
        boardsQuery.error instanceof Error ? boardsQuery.error : null,
      createBoard: async (input: CreateBoardInput) => {
        await createBoardMutation.mutateAsync(input);
      },
      currentParams,
      isCreatingBoard: createBoardMutation.isPending,
      isLoadingBoards: boardsQuery.isLoading,
      queryBoards: (params: QueryBoardsParams) => {
        setCurrentParams(currentState => {
          return normalizeQueryBoardsParams({
            ...currentState,
            ...params
          });
        });
      }
    }),
    [
      boardsQuery.data,
      boardsQuery.error,
      boardsQuery.isLoading,
      createBoardMutation,
      currentParams
    ]
  );

  return <BoardsContext.Provider value={value}>{children}</BoardsContext.Provider>;
};
