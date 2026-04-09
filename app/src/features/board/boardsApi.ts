import type { BoardOwnerOption, QueryBoardsParams } from "@contexts/boards-context";
import type { Board } from "@models/board";

export const BOARDS_PER_PAGE = 15;

const normalizeString = (value: string): string => {
  return value.trim().toLowerCase();
};

/**
 * Builds a stable owner option list for the boards toolbar from the full board collection.
 * This is used by the provider so owner filters stay available even when the visible board list
 * is already narrowed by search or owner query params.
 */
export const getBoardOwnerOptions = (boards: Board[]): BoardOwnerOption[] => {
  const ownerOptionMap = new Map<string, BoardOwnerOption>();

  boards.forEach(board => {
    ownerOptionMap.set(board.ownerUserId, {
      label: getBoardOwnerOptionLabel(board),
      ownerUserId: board.ownerUserId
    });
  });

  return [...ownerOptionMap.values()].sort((left, right) => {
    return left.label.localeCompare(right.label);
  });
};

/**
 * Resolves a human-readable owner label from the nested board owner data.
 * This is used when composing owner filter options for the boards toolbar.
 */
export const getBoardOwnerOptionLabel = (board: Board): string => {
  const fullName = [board.owner?.firstName ?? "", board.owner?.lastName ?? ""]
    .map(part => part.trim())
    .filter(part => part.length > 0)
    .join(" ")
    .trim();

  return fullName.length > 0 ? fullName : "Unknown owner";
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

/**
 * Applies client-side board filtering for state that is still local to the page, such as
 * active/inactive tabs and pagination. Keyword and owner can also be applied here when the caller
 * is working with an already fetched collection.
 */
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
