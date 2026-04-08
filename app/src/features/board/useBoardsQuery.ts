import { useQuery } from "@tanstack/react-query";
import type { UseQueryResult } from "@tanstack/react-query";

import type { QueryBoardsParams } from "@contexts/boards-context";
import { queryDashboardsRequest } from "@features/board/boardsApi";
import { mapDashboardResponseToBoard } from "@models/board";
import type { Board } from "@models/board";

export const useBoardsQuery = (
  params: QueryBoardsParams
): UseQueryResult<Board[]> => {
  return useQuery({
    queryFn: async () => {
      const dashboards = await queryDashboardsRequest(params);

      return dashboards.map(mapDashboardResponseToBoard);
    },
    queryKey: ["boards", params]
  });
};
