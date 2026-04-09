import { useContext } from "react";

import { BoardContext } from "@contexts/board-context";
import type { BoardContextValue } from "@contexts/board-context";

export const useBoard = (): BoardContextValue => {
  return useContext(BoardContext);
};
