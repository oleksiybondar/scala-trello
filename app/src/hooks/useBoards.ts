import { useContext } from "react";

import { BoardsContext } from "@contexts/boards-context";
import type { BoardsContextValue } from "@contexts/boards-context";

export const useBoards = (): BoardsContextValue => {
  return useContext(BoardsContext);
};
