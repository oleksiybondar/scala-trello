import { useQuery } from "@tanstack/react-query";
import type { UseQueryResult } from "@tanstack/react-query";

import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import { buildBoardQuery, mapBoardResponseToBoard } from "@models/board";
import type { Board } from "@models/board";
import type { BoardQueryResponse } from "@models/board";

export const useBoardQuery = (boardId: string): UseQueryResult<Board | null> => {
  const { accessToken, session } = useAuth();

  return useQuery({
    enabled: accessToken !== null && session !== null && boardId.length > 0,
    queryFn: async () => {
      const response = await requestGraphQL<BoardQueryResponse>({
        accessToken,
        document: buildBoardQuery(boardId),
        ...(session?.tokenType === undefined
          ? {}
          : {
              tokenType: session.tokenType
            })
      });

      return response.board === null ? null : mapBoardResponseToBoard(response.board);
    },
    queryKey: ["board", boardId]
  });
};
