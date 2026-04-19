import { createContext } from "react";

import type { CreateBoardInput } from "../domain/board/graphql";
import type { Board } from "../domain/board/graphql";

export interface BoardOwnerOption {
  label: string;
  ownerUserId: string;
}

export interface QueryBoardsParams {
  active?: boolean | undefined;
  keyword?: string | undefined;
  owner?: string | undefined;
  page?: number | undefined;
  showInactive?: boolean | undefined;
}

export interface NormalizedQueryBoardsParams {
  keyword?: string | undefined;
  owner?: string | undefined;
  page: number;
  showInactive: boolean;
}

export interface BoardsContextValue {
  boards: Board[];
  canLoadMoreBoards: boolean;
  boardsError: Error | null;
  createBoard: (input: CreateBoardInput) => Promise<void>;
  currentParams: NormalizedQueryBoardsParams;
  isCreatingBoard: boolean;
  isLoadingBoards: boolean;
  isLoadingMoreBoards: boolean;
  loadNextBoardsPage: () => Promise<void>;
  ownerOptions: BoardOwnerOption[];
  queryBoards: (params: QueryBoardsParams) => void;
}

const missingBoardsProvider = (): never => {
  throw new Error("BoardsContext is missing its provider.");
};

export const BoardsContext = createContext<BoardsContextValue>({
  boards: [],
  canLoadMoreBoards: false,
  boardsError: null,
  createBoard: missingBoardsProvider,
  currentParams: {
    page: 1,
    showInactive: false
  },
  isCreatingBoard: false,
  isLoadingBoards: false,
  isLoadingMoreBoards: false,
  loadNextBoardsPage: missingBoardsProvider,
  ownerOptions: [],
  queryBoards: missingBoardsProvider
});
