import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { UseMutationResult } from "@tanstack/react-query";

import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildCreateDashboardMutation,
  mapCreateBoardInputToRequest,
  mapDashboardResponseToBoard
} from "@models/board";
import type { Board, CreateBoardInput } from "@models/board";
import type { CreateDashboardMutationResponse } from "@models/board";

export const useCreateBoardMutation = (): UseMutationResult<
  Board,
  Error,
  CreateBoardInput
> => {
  const { accessToken, session } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: CreateBoardInput) => {
      if (accessToken === null || session === null) {
        throw new Error("Authentication context is required to create a board.");
      }

      const response = await requestGraphQL<CreateDashboardMutationResponse>({
        accessToken,
        document: buildCreateDashboardMutation(mapCreateBoardInputToRequest(input)),
        tokenType: session.tokenType
      });

      return mapDashboardResponseToBoard(response.createDashboard);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["boards"]
      });
    }
  });
};
