import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { UseMutationResult } from "@tanstack/react-query";

import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildChangeBoardMemberRoleMutation,
  buildRemoveBoardMemberMutation,
  mapBoardMemberResponseToBoardMember
} from "@models/board";
import type {
  BoardMember,
  ChangeBoardMemberRoleMutationResponse,
  RemoveBoardMemberMutationResponse
} from "@models/board";

interface ChangeBoardMemberRoleInput {
  boardId: string;
  roleId: string;
  userId: string;
}

interface RemoveBoardMemberInput {
  boardId: string;
  userId: string;
}

interface BoardMemberMutationHelpers {
  changeBoardMemberRoleMutation: UseMutationResult<
    BoardMember,
    Error,
    ChangeBoardMemberRoleInput
  >;
  removeBoardMemberMutation: UseMutationResult<boolean, Error, RemoveBoardMemberInput>;
}

export const useBoardMemberMutation = (): BoardMemberMutationHelpers => {
  const { accessToken, session } = useAuth();
  const queryClient = useQueryClient();

  const invalidateBoardQueries = async (boardId: string): Promise<void> => {
    await Promise.all([
      queryClient.invalidateQueries({
        queryKey: ["board", boardId]
      }),
      queryClient.invalidateQueries({
        queryKey: ["board-members", boardId]
      }),
      queryClient.invalidateQueries({
        queryKey: ["boards"]
      })
    ]);
  };

  return {
    changeBoardMemberRoleMutation: useMutation({
      mutationFn: async ({ boardId, roleId, userId }) => {
        if (accessToken === null || session === null) {
          throw new Error("Authentication context is required to update a board member.");
        }

        const response = await requestGraphQL<ChangeBoardMemberRoleMutationResponse>({
          accessToken,
          document: buildChangeBoardMemberRoleMutation(boardId, userId, roleId),
          tokenType: session.tokenType
        });

        return mapBoardMemberResponseToBoardMember(response.changeBoardMemberRole);
      },
      onSuccess: async (_, variables) => {
        await invalidateBoardQueries(variables.boardId);
      }
    }),
    removeBoardMemberMutation: useMutation({
      mutationFn: async ({ boardId, userId }) => {
        if (accessToken === null || session === null) {
          throw new Error("Authentication context is required to update a board member.");
        }

        const response = await requestGraphQL<RemoveBoardMemberMutationResponse>({
          accessToken,
          document: buildRemoveBoardMemberMutation(boardId, userId),
          tokenType: session.tokenType
        });

        return response.removeBoardMember;
      },
      onSuccess: async (_, variables) => {
        await invalidateBoardQueries(variables.boardId);
      }
    })
  };
};
