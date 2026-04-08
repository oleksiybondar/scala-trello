import type { Board } from "@models/board";

import type { QueryBoardsParams } from "@contexts/boards-context";

export const BOARDS_PER_PAGE = 15;

const normalizeString = (value: string): string => {
  return value.trim().toLowerCase();
};

const matchesKeyword = (
  board: Board,
  keyword: string | undefined
): boolean => {
  if (keyword === undefined || keyword.trim().length === 0) {
    return true;
  }

  const normalizedKeyword = normalizeString(keyword);

  return (
    normalizeString(board.name).includes(normalizedKeyword) ||
    normalizeString(board.description ?? "").includes(normalizedKeyword)
  );
};

export const filterBoards = (
  boards: Board[],
  {
    active = true,
    keyword,
    owner,
    page = 1
  }: QueryBoardsParams
): Board[] => {
  const filteredBoards = boards
    .filter(board => {
      if (board.active !== active) {
        return false;
      }

      if (owner !== undefined && board.ownerUserId !== owner) {
        return false;
      }

      return matchesKeyword(board, keyword);
    })
    .sort((left, right) => {
      return right.modifiedAt.localeCompare(left.modifiedAt);
    });

  const offset = (page - 1) * BOARDS_PER_PAGE;

  return filteredBoards.slice(offset, offset + BOARDS_PER_PAGE);
};
