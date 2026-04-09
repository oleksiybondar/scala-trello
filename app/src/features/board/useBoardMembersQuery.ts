import { useQuery } from "@tanstack/react-query";
import type { UseQueryResult } from "@tanstack/react-query";

import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildBoardMembersQuery,
  mapBoardMemberResponseToBoardMember
} from "@models/board";
import type { BoardMember } from "@models/board";
import type { BoardMembersQueryResponse } from "@models/board";

export const useBoardMembersQuery = (
  boardId: string
): UseQueryResult<BoardMember[]> => {
  const { accessToken, session } = useAuth();

  return useQuery({
    enabled: accessToken !== null && session !== null && boardId.length > 0,
    queryFn: async () => {
      const response = await requestGraphQL<BoardMembersQueryResponse>({
        accessToken,
        document: buildBoardMembersQuery(boardId),
        ...(session?.tokenType === undefined
          ? {}
          : {
              tokenType: session.tokenType
            })
      });

      return response.dashboardMembers.map(mapBoardMemberResponseToBoardMember);
    },
    queryKey: ["board-members", boardId]
  });
};
