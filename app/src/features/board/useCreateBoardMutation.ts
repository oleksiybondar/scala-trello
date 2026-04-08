import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { UseMutationResult } from "@tanstack/react-query";

import { createDashboardRequest } from "@features/board/boardsApi";
import { mapCreateBoardInputToRequest, mapDashboardResponseToBoard } from "@models/board";
import type { Board, CreateBoardInput } from "@models/board";

export const useCreateBoardMutation = (
  userId: string
): UseMutationResult<Board, Error, CreateBoardInput> => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: CreateBoardInput) => {
      const dashboard = await createDashboardRequest(
        mapCreateBoardInputToRequest(input),
        userId
      );

      return mapDashboardResponseToBoard(dashboard);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["boards"]
      });
    }
  });
};
