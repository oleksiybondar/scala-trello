import type { PropsWithChildren, ReactElement } from "react";
import { useState } from "react";

import { BoardsContext } from "@contexts/boards-context";
import type {
  NormalizedQueryBoardsParams,
  QueryBoardsParams
} from "@contexts/boards-context";
import { filterBoards, getBoardOwnerOptions } from "../domain/board/boardsApi";
import { useBoardsService } from "../domain/board/useBoardsService";
import type { CreateBoardInput } from "../domain/board/graphql";

const DEFAULT_QUERY_BOARDS_PARAMS = {
  page: 1,
  showInactive: false
} as const;

const normalizeQueryBoardsParams = (
  params: QueryBoardsParams
): NormalizedQueryBoardsParams => {
  return {
    keyword: params.keyword,
    owner: params.owner,
    page: params.page ?? DEFAULT_QUERY_BOARDS_PARAMS.page,
    showInactive: params.showInactive ?? DEFAULT_QUERY_BOARDS_PARAMS.showInactive
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
  const {
    boards: visibleBoards,
    boardsError,
    createBoard,
    isCreatingBoard,
    isLoadingBoards
  } = useBoardsService({
    currentParams
  });

  const value = {
    boards: filterBoards(visibleBoards, {
      page: currentParams.page
    }),
    boardsError,
    createBoard: async (input: CreateBoardInput) => {
      await createBoard(input);
    },
    currentParams,
    isCreatingBoard,
    isLoadingBoards,
    ownerOptions: getBoardOwnerOptions(visibleBoards),
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
