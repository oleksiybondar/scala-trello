import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { UseMutationResult } from "@tanstack/react-query";

import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildActivateBoardMutation,
  buildChangeBoardDescriptionMutation,
  buildChangeBoardOwnershipMutation,
  buildChangeBoardTitleMutation,
  buildDeactivateBoardMutation,
  mapBoardResponseToBoard
} from "@models/board";
import type {
  ActivateBoardMutationResponse,
  Board,
  ChangeBoardDescriptionMutationResponse,
  ChangeBoardOwnershipMutationResponse,
  ChangeBoardTitleMutationResponse,
  DeactivateBoardMutationResponse
} from "@models/board";

interface ChangeBoardDescriptionInput {
  boardId: string;
  description: string | null;
}

interface ChangeBoardOwnershipInput {
  boardId: string;
  owner: string;
}

interface ChangeBoardTitleInput {
  boardId: string;
  title: string;
}

interface DeactivateBoardInput {
  boardId: string;
}

interface BoardMetadataMutationHelpers {
  activateBoardMutation: UseMutationResult<Board, Error, DeactivateBoardInput>;
  changeBoardDescriptionMutation: UseMutationResult<
    Board,
    Error,
    ChangeBoardDescriptionInput
  >;
  changeBoardOwnershipMutation: UseMutationResult<
    Board,
    Error,
    ChangeBoardOwnershipInput
  >;
  changeBoardTitleMutation: UseMutationResult<Board, Error, ChangeBoardTitleInput>;
  deactivateBoardMutation: UseMutationResult<Board, Error, DeactivateBoardInput>;
}

export const useBoardMetadataMutation = (): BoardMetadataMutationHelpers => {
  const { accessToken, session } = useAuth();
  const queryClient = useQueryClient();

  const invalidateBoardQueries = async (boardId: string): Promise<void> => {
    await Promise.all([
      queryClient.invalidateQueries({
        queryKey: ["board", boardId]
      }),
      queryClient.invalidateQueries({
        queryKey: ["boards"]
      })
    ]);
  };

  return {
    activateBoardMutation: useMutation({
      mutationFn: async ({ boardId }) => {
        if (accessToken === null || session === null) {
          throw new Error("Authentication context is required to update a board.");
        }

        const response = await requestGraphQL<ActivateBoardMutationResponse>({
          accessToken,
          document: buildActivateBoardMutation(boardId),
          tokenType: session.tokenType
        });

        return mapBoardResponseToBoard(response.activateBoard);
      },
      onSuccess: async (_, variables) => {
        await invalidateBoardQueries(variables.boardId);
      }
    }),
    changeBoardDescriptionMutation: useMutation({
      mutationFn: async ({ boardId, description }) => {
        if (accessToken === null || session === null) {
          throw new Error("Authentication context is required to update a board.");
        }

        const response = await requestGraphQL<ChangeBoardDescriptionMutationResponse>({
          accessToken,
          document: buildChangeBoardDescriptionMutation(boardId, description),
          tokenType: session.tokenType
        });

        return mapBoardResponseToBoard(response.changeBoardDescription);
      },
      onSuccess: async (_, variables) => {
        await invalidateBoardQueries(variables.boardId);
      }
    }),
    changeBoardOwnershipMutation: useMutation({
      mutationFn: async ({ boardId, owner }) => {
        if (accessToken === null || session === null) {
          throw new Error("Authentication context is required to update a board.");
        }

        const response = await requestGraphQL<ChangeBoardOwnershipMutationResponse>({
          accessToken,
          document: buildChangeBoardOwnershipMutation(boardId, owner),
          tokenType: session.tokenType
        });

        return mapBoardResponseToBoard(response.changeBoardOwnership);
      },
      onSuccess: async (_, variables) => {
        await invalidateBoardQueries(variables.boardId);
      }
    }),
    changeBoardTitleMutation: useMutation({
      mutationFn: async ({ boardId, title }) => {
        if (accessToken === null || session === null) {
          throw new Error("Authentication context is required to update a board.");
        }

        const response = await requestGraphQL<ChangeBoardTitleMutationResponse>({
          accessToken,
          document: buildChangeBoardTitleMutation(boardId, title),
          tokenType: session.tokenType
        });

        return mapBoardResponseToBoard(response.changeBoardTitle);
      },
      onSuccess: async (_, variables) => {
        await invalidateBoardQueries(variables.boardId);
      }
    }),
    deactivateBoardMutation: useMutation({
      mutationFn: async ({ boardId }) => {
        if (accessToken === null || session === null) {
          throw new Error("Authentication context is required to update a board.");
        }

        const response = await requestGraphQL<DeactivateBoardMutationResponse>({
          accessToken,
          document: buildDeactivateBoardMutation(boardId),
          tokenType: session.tokenType
        });

        return mapBoardResponseToBoard(response.deactivateBoard);
      },
      onSuccess: async (_, variables) => {
        await invalidateBoardQueries(variables.boardId);
      }
    })
  };
};
