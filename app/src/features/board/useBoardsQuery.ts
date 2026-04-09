import { useQuery } from "@tanstack/react-query";
import type { UseQueryResult } from "@tanstack/react-query";

import type { QueryBoardsParams } from "@contexts/boards-context";
import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildMyBoardsQuery,
  mapBoardResponseToBoard
} from "@models/board";
import type { Board } from "@models/board";
import type { MyBoardsQueryResponse } from "@models/board";

export const useBoardsQuery = (
  params: QueryBoardsParams
): UseQueryResult<Board[]> => {
  const { accessToken, session } = useAuth();

  return useQuery({
    enabled: accessToken !== null && session !== null,
    queryFn: async () => {
      const response = await requestGraphQL<MyBoardsQueryResponse>({
        accessToken,
        document: buildMyBoardsQuery({
          keyword: params.keyword,
          ownerUserId: params.owner
        }),
        ...(session?.tokenType === undefined
          ? {}
          : {
              tokenType: session.tokenType
            })
      });
      return response.myBoards.map(mapBoardResponseToBoard);
    },
    queryKey: ["boards", params.keyword ?? "", params.owner ?? ""]
  });
};
