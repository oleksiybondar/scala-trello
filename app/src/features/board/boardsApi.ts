import type { DashboardResponse } from "@models/board";
import type { CreateBoardRequest } from "@models/board";

import type { QueryBoardsParams } from "@contexts/boards-context";

const dashboardStore: DashboardResponse[] = [];

const normalizeString = (value: string): string => {
  return value.trim().toLowerCase();
};

const matchesKeyword = (
  dashboard: DashboardResponse,
  keyword: string | undefined
): boolean => {
  if (keyword === undefined || keyword.trim().length === 0) {
    return true;
  }

  const normalizedKeyword = normalizeString(keyword);

  return (
    normalizeString(dashboard.name).includes(normalizedKeyword) ||
    normalizeString(dashboard.description ?? "").includes(normalizedKeyword)
  );
};

export const BOARDS_PER_PAGE = 15;

export const queryDashboardsRequest = ({
  active = true,
  keyword,
  owner,
  page = 1
}: QueryBoardsParams): Promise<DashboardResponse[]> => {
  const filteredDashboards = dashboardStore
    .filter(dashboard => {
      if (dashboard.active !== active) {
        return false;
      }

      if (owner !== undefined && dashboard.owner_user_id !== owner) {
        return false;
      }

      return matchesKeyword(dashboard, keyword);
    })
    .sort((left, right) => {
      return right.modified_at.localeCompare(left.modified_at);
    });

  const offset = (page - 1) * BOARDS_PER_PAGE;

  return Promise.resolve(
    filteredDashboards.slice(offset, offset + BOARDS_PER_PAGE)
  );
};

export const createDashboardRequest = (
  request: CreateBoardRequest,
  userId: string
): Promise<DashboardResponse> => {
  const now = new Date().toISOString();
  const dashboard: DashboardResponse = {
    active: true,
    created_at: now,
    created_by_user_id: userId,
    description: request.description ?? null,
    id: crypto.randomUUID(),
    last_modified_by_user_id: userId,
    modified_at: now,
    name: request.name,
    owner_user_id: userId
  };

  dashboardStore.unshift(dashboard);

  return Promise.resolve(dashboard);
};

export const resetDashboardStore = (): void => {
  dashboardStore.length = 0;
};
