import { useQuery } from "@tanstack/react-query";
import type { UseQueryResult } from "@tanstack/react-query";

import type { QueryBoardsParams } from "@contexts/boards-context";
import { filterBoards } from "@features/board/boardsApi";
import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildMyDashboardsQuery,
  mapDashboardResponseToBoard
} from "@models/board";
import type { Board } from "@models/board";
import type { MyDashboardsQueryResponse } from "@models/board";

export const useBoardsQuery = (
  params: QueryBoardsParams
): UseQueryResult<Board[]> => {
  const { accessToken, session } = useAuth();

  return useQuery({
    enabled: accessToken !== null && session !== null,
    queryFn: async () => {
      const response = await requestGraphQL<MyDashboardsQueryResponse>({
        accessToken,
        document: buildMyDashboardsQuery(),
        ...(session?.tokenType === undefined
          ? {}
          : {
              tokenType: session.tokenType
            })
      });
      const boards = response.myDashboards.map(mapDashboardResponseToBoard);

      return filterBoards(boards, params);
    },
    queryKey: ["boards", params]
  });
};
