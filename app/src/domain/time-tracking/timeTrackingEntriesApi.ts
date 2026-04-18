import type { QueryTimeTrackingEntriesParams } from "@contexts/time-tracking-entries-context";
import type { TimeTrackingEntry } from "./graphql";

export const TIME_TRACKING_ENTRIES_PER_PAGE = 20;

const filterBySearch = (entry: TimeTrackingEntry, keyword: string | undefined): boolean => {
  if (keyword === undefined || keyword.trim().length === 0) {
    return true;
  }

  const normalizedKeyword = keyword.trim().toLowerCase();

  return [
    entry.activityName,
    entry.activityCode,
    entry.description,
    entry.ticket?.title,
    entry.ticket?.description,
    entry.ticket?.board?.title,
    entry.user === null ? null : `${entry.user.firstName} ${entry.user.lastName}`
  ].some(value => {
    return value?.toLowerCase().includes(normalizedKeyword) ?? false;
  });
};

const filterByActivities = (entry: TimeTrackingEntry, activityIds: string[] | undefined): boolean => {
  if (activityIds === undefined || activityIds.length === 0) {
    return true;
  }

  return activityIds.includes(entry.activityId);
};

const filterByBoards = (entry: TimeTrackingEntry, boardIds: string[] | undefined): boolean => {
  if (boardIds === undefined || boardIds.length === 0) {
    return true;
  }

  const boardId = entry.ticket?.board?.boardId;

  return boardId !== undefined && boardIds.includes(boardId);
};

const filterByBoardState = (
  entry: TimeTrackingEntry,
  boardState: QueryTimeTrackingEntriesParams["boardState"]
): boolean => {
  if (boardState === undefined || boardState === "all") {
    return true;
  }

  const isActive = entry.ticket?.board?.active;

  if (isActive === undefined) {
    return false;
  }

  return boardState === "active" ? isActive : !isActive;
};

const applyFilters = (
  entries: TimeTrackingEntry[],
  params: QueryTimeTrackingEntriesParams
): TimeTrackingEntry[] => {
  return entries
    .filter(entry => filterBySearch(entry, params.keyword))
    .filter(entry => filterByActivities(entry, params.activityIds))
    .filter(entry => filterByBoards(entry, params.boardIds))
    .filter(entry => filterByBoardState(entry, params.boardState));
};

export const filterTimeTrackingEntries = (
  entries: TimeTrackingEntry[],
  params: QueryTimeTrackingEntriesParams
): TimeTrackingEntry[] => {
  const page = params.page ?? 1;
  const filteredEntries = applyFilters(entries, params).sort((left, right) => {
    return right.loggedAt.localeCompare(left.loggedAt);
  });
  const offset = (page - 1) * TIME_TRACKING_ENTRIES_PER_PAGE;

  return filteredEntries.slice(offset, offset + TIME_TRACKING_ENTRIES_PER_PAGE);
};

export const countTimeTrackingEntries = (
  entries: TimeTrackingEntry[],
  params: QueryTimeTrackingEntriesParams
): number => {
  return applyFilters(entries, params).length;
};
