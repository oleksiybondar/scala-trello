import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { UseMutationResult } from "@tanstack/react-query";

import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildInviteBoardMemberMutation,
  mapBoardMemberResponseToBoardMember
} from "@models/board";
import type { BoardMember, InviteBoardMemberMutationResponse } from "@models/board";

interface InviteBoardMemberInput {
  boardId: string;
  roleId: string;
  user: string;
}

export const useInviteBoardMemberMutation = (): UseMutationResult<
  BoardMember,
  Error,
  InviteBoardMemberInput
> => {
  const { accessToken, session } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ boardId, roleId, user }) => {
      if (accessToken === null || session === null) {
        throw new Error("Authentication context is required to invite a board member.");
      }

      const response = await requestGraphQL<InviteBoardMemberMutationResponse>({
        accessToken,
        document: buildInviteBoardMemberMutation(boardId, user, roleId),
        tokenType: session.tokenType
      });

      return mapBoardMemberResponseToBoardMember(response.inviteBoardMember);
    },
    onSuccess: async (_, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["board", variables.boardId]
        }),
        queryClient.invalidateQueries({
          queryKey: ["board-members", variables.boardId]
        }),
        queryClient.invalidateQueries({
          queryKey: ["boards"]
        })
      ]);
    }
  });
};
