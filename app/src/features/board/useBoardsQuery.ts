import { useQuery } from "@tanstack/react-query";
import type { UseQueryResult } from "@tanstack/react-query";

import type { QueryBoardsParams } from "@contexts/boards-context";
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
        document: buildMyDashboardsQuery({
          keyword: params.keyword,
          ownerUserId: params.owner
        }),
        ...(session?.tokenType === undefined
          ? {}
          : {
              tokenType: session.tokenType
            })
      });
      return response.myDashboards.map(mapDashboardResponseToBoard);
    },
    queryKey: ["boards", params.keyword ?? "", params.owner ?? ""]
  });
};
