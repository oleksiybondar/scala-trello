import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import type { NormalizedQueryBoardsParams } from "@contexts/boards-context";
import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildCreateBoardMutation,
  buildMyBoardsQuery,
  mapBoardResponseToBoard,
  mapCreateBoardInputToRequest
} from "./graphql";
import type {
  Board,
  CreateBoardInput,
  CreateBoardMutationResponse,
  MyBoardsQueryResponse
} from "./graphql";

interface BoardsService {
  boards: Board[];
  boardsError: Error | null;
  createBoard: (input: CreateBoardInput) => Promise<Board>;
  isCreatingBoard: boolean;
  isLoadingBoards: boolean;
}

interface UseBoardsServiceParams {
  currentParams: NormalizedQueryBoardsParams;
}

export const useBoardsService = ({
  currentParams
}: UseBoardsServiceParams): BoardsService => {
  const { accessToken, session } = useAuth();
  const queryClient = useQueryClient();
  const isEnabled = accessToken !== null && session !== null;

  const boardsQuery = useQuery({
    enabled: isEnabled,
    queryFn: async () => {
      if (accessToken === null || session === null) {
        throw new Error("Authentication context is required for board operations.");
      }

      const response = await requestGraphQL<MyBoardsQueryResponse>({
        accessToken,
        document: buildMyBoardsQuery({
          active: currentParams.showInactive ? undefined : true,
          keyword: currentParams.keyword,
          ownerUserId: currentParams.owner
        }),
        tokenType: session.tokenType
      });

      return response.myBoards.map(mapBoardResponseToBoard);
    },
    queryKey: [
      "boards",
      currentParams.showInactive ? "all" : "active",
      currentParams.keyword ?? "",
      currentParams.owner ?? ""
    ]
  });

  const createBoardMutation = useMutation({
    mutationFn: async (input: CreateBoardInput) => {
      const response = await requestGraphQL<CreateBoardMutationResponse>({
        accessToken,
        document: buildCreateBoardMutation(mapCreateBoardInputToRequest(input)),
      });

      return mapBoardResponseToBoard(response.createBoard);
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["boards"]
        }),
        queryClient.invalidateQueries({
          queryKey: ["board"]
        })
      ]);
    }
  });

  return {
    boards: boardsQuery.data ?? [],
    boardsError: boardsQuery.error instanceof Error ? boardsQuery.error : null,
    createBoard: async (input: CreateBoardInput) => {
      return createBoardMutation.mutateAsync(input);
    },
    isCreatingBoard: createBoardMutation.isPending,
    isLoadingBoards: boardsQuery.isLoading
  };
};
