import { createContext } from "react";

import type { Board } from "@models/board";

export interface BoardContextValue {
  activateBoard: () => Promise<void>;
  board: Board | null;
  boardError: Error | null;
  changeBoardDescription: (description: string | null) => Promise<void>;
  changeBoardOwnership: (owner: string) => Promise<void>;
  changeBoardTitle: (title: string) => Promise<void>;
  deactivateBoard: () => Promise<void>;
  isLoadingBoard: boolean;
  isUpdatingBoardDescription: boolean;
  isUpdatingBoardOwnership: boolean;
  isUpdatingBoardStatus: boolean;
  isUpdatingBoardTitle: boolean;
}

const missingBoardProvider = (): never => {
  throw new Error("BoardContext is missing its provider.");
};

export const BoardContext = createContext<BoardContextValue>({
  activateBoard: missingBoardProvider,
  board: null,
  boardError: null,
  changeBoardDescription: missingBoardProvider,
  changeBoardOwnership: missingBoardProvider,
  changeBoardTitle: missingBoardProvider,
  deactivateBoard: missingBoardProvider,
  isLoadingBoard: false,
  isUpdatingBoardDescription: false,
  isUpdatingBoardOwnership: false,
  isUpdatingBoardStatus: false,
  isUpdatingBoardTitle: false
});
