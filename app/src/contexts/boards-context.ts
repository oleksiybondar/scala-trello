import { createContext } from "react";

import type { CreateBoardInput } from "@models/board";
import type { Board } from "@models/board";

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
  boardsError: Error | null;
  createBoard: (input: CreateBoardInput) => Promise<void>;
  currentParams: NormalizedQueryBoardsParams;
  isCreatingBoard: boolean;
  isLoadingBoards: boolean;
  ownerOptions: BoardOwnerOption[];
  queryBoards: (params: QueryBoardsParams) => void;
}

const missingBoardsProvider = (): never => {
  throw new Error("BoardsContext is missing its provider.");
};

export const BoardsContext = createContext<BoardsContextValue>({
  boards: [],
  boardsError: null,
  createBoard: missingBoardsProvider,
  currentParams: {
    page: 1,
    showInactive: false
  },
  isCreatingBoard: false,
  isLoadingBoards: false,
  ownerOptions: [],
  queryBoards: missingBoardsProvider
});
