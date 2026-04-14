import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildActivateBoardMutation,
  buildBoardMembersQuery,
  buildBoardQuery,
  buildChangeBoardDescriptionMutation,
  buildChangeBoardMemberRoleMutation,
  buildChangeBoardOwnershipMutation,
  buildChangeBoardTitleMutation,
  buildDeactivateBoardMutation,
  buildInviteBoardMemberMutation,
  buildRemoveBoardMemberMutation,
  mapBoardMemberResponseToBoardMember,
  mapBoardResponseToBoard
} from "./graphql";
import type {
  ActivateBoardMutationResponse,
  Board,
  BoardMember,
  BoardMembersQueryResponse,
  BoardQueryResponse,
  ChangeBoardDescriptionMutationResponse,
  ChangeBoardMemberRoleMutationResponse,
  ChangeBoardOwnershipMutationResponse,
  ChangeBoardTitleMutationResponse,
  DeactivateBoardMutationResponse,
  InviteBoardMemberMutationResponse,
  RemoveBoardMemberMutationResponse
} from "./graphql";

interface BoardService {
  activateBoard: () => Promise<Board>;
  board: Board | null;
  boardError: Error | null;
  changeBoardDescription: (description: string | null) => Promise<Board>;
  changeBoardMemberRole: (userId: string, roleId: string) => Promise<BoardMember>;
  changeBoardOwnership: (owner: string) => Promise<Board>;
  changeBoardTitle: (title: string) => Promise<Board>;
  deactivateBoard: () => Promise<Board>;
  inviteBoardMember: (user: string, roleId: string) => Promise<BoardMember>;
  isInvitingBoardMember: boolean;
  isLoadingBoard: boolean;
  isLoadingMembers: boolean;
  isRemovingBoardMember: boolean;
  isUpdatingBoardDescription: boolean;
  isUpdatingBoardMemberRole: boolean;
  isUpdatingBoardOwnership: boolean;
  isUpdatingBoardStatus: boolean;
  isUpdatingBoardTitle: boolean;
  members: BoardMember[];
  membersError: Error | null;
  removeBoardMember: (userId: string) => Promise<boolean>;
}

interface UseBoardServiceParams {
  boardId: string;
}

const createBoardQueryKey = (boardId: string): [string, string] => ["board", boardId];
const createBoardMembersQueryKey = (boardId: string): [string, string] => [
  "board-members",
  boardId
];

const loadBoard = async (
  accessToken: string,
  tokenType: string,
  boardId: string
): Promise<Board | null> => {
  const response = await requestGraphQL<BoardQueryResponse>({
    accessToken,
    document: buildBoardQuery(boardId),
    tokenType
  });

  return response.board === null ? null : mapBoardResponseToBoard(response.board);
};

const loadBoardMembers = async (
  accessToken: string,
  tokenType: string,
  boardId: string
): Promise<BoardMember[]> => {
  const response = await requestGraphQL<BoardMembersQueryResponse>({
    accessToken,
    document: buildBoardMembersQuery(boardId),
    tokenType
  });

  return response.boardMembers.map(mapBoardMemberResponseToBoardMember);
};

export const useBoardService = ({ boardId }: UseBoardServiceParams): BoardService => {
  const { accessToken, session } = useAuth();
  const queryClient = useQueryClient();
  const isEnabled = accessToken !== null && session !== null && boardId.length > 0;

  const boardQuery = useQuery({
    enabled: isEnabled,
    queryFn: async () => {
      if (accessToken === null || session === null) {
        throw new Error("Authentication context is required for board operations.");
      }

      return loadBoard(accessToken, session.tokenType, boardId);
    },
    queryKey: createBoardQueryKey(boardId)
  });

  const membersQuery = useQuery({
    enabled: isEnabled,
    queryFn: async () => {
      if (accessToken === null || session === null) {
        throw new Error("Authentication context is required for board operations.");
      }

      return loadBoardMembers(accessToken, session.tokenType, boardId);
    },
    queryKey: createBoardMembersQueryKey(boardId)
  });

  const invalidateBoardQueries = async (): Promise<void> => {
    await Promise.all([
      queryClient.invalidateQueries({
        queryKey: createBoardQueryKey(boardId)
      }),
      queryClient.invalidateQueries({
        queryKey: createBoardMembersQueryKey(boardId)
      }),
      queryClient.invalidateQueries({
        queryKey: ["boards"]
      })
    ]);
  };

  const activateBoardMutation = useMutation({
    mutationFn: async () => {
      const response = await requestGraphQL<ActivateBoardMutationResponse>({
        accessToken,
        document: buildActivateBoardMutation(boardId),
      });

      return mapBoardResponseToBoard(response.activateBoard);
    },
    onSuccess: async () => {
      await invalidateBoardQueries();
    }
  });

  const changeBoardDescriptionMutation = useMutation({
    mutationFn: async (description: string | null) => {
      const response = await requestGraphQL<ChangeBoardDescriptionMutationResponse>({
        accessToken,
        document: buildChangeBoardDescriptionMutation(boardId, description),
      });

      return mapBoardResponseToBoard(response.changeBoardDescription);
    },
    onSuccess: async () => {
      await invalidateBoardQueries();
    }
  });

  const changeBoardMemberRoleMutation = useMutation({
    mutationFn: async ({
      roleId,
      userId
    }: {
      roleId: string;
      userId: string;
    }) => {
      const response = await requestGraphQL<ChangeBoardMemberRoleMutationResponse>({
        accessToken,
        document: buildChangeBoardMemberRoleMutation(boardId, userId, roleId),
      });

      return mapBoardMemberResponseToBoardMember(response.changeBoardMemberRole);
    },
    onSuccess: async () => {
      await invalidateBoardQueries();
    }
  });

  const changeBoardOwnershipMutation = useMutation({
    mutationFn: async (owner: string) => {
      const response = await requestGraphQL<ChangeBoardOwnershipMutationResponse>({
        accessToken,
        document: buildChangeBoardOwnershipMutation(boardId, owner),
      });

      return mapBoardResponseToBoard(response.changeBoardOwnership);
    },
    onSuccess: async () => {
      await invalidateBoardQueries();
    }
  });

  const changeBoardTitleMutation = useMutation({
    mutationFn: async (title: string) => {
      const response = await requestGraphQL<ChangeBoardTitleMutationResponse>({
        accessToken,
        document: buildChangeBoardTitleMutation(boardId, title),
      });

      return mapBoardResponseToBoard(response.changeBoardTitle);
    },
    onSuccess: async () => {
      await invalidateBoardQueries();
    }
  });

  const deactivateBoardMutation = useMutation({
    mutationFn: async () => {
      const response = await requestGraphQL<DeactivateBoardMutationResponse>({
        accessToken,
        document: buildDeactivateBoardMutation(boardId),
      });

      return mapBoardResponseToBoard(response.deactivateBoard);
    },
    onSuccess: async () => {
      await invalidateBoardQueries();
    }
  });

  const inviteBoardMemberMutation = useMutation({
    mutationFn: async ({ roleId, user }: { roleId: string; user: string }) => {
      const response = await requestGraphQL<InviteBoardMemberMutationResponse>({
        accessToken,
        document: buildInviteBoardMemberMutation(boardId, user, roleId),
      });

      return mapBoardMemberResponseToBoardMember(response.inviteBoardMember);
    },
    onSuccess: async () => {
      await invalidateBoardQueries();
    }
  });

  const removeBoardMemberMutation = useMutation({
    mutationFn: async (userId: string) => {
      const response = await requestGraphQL<RemoveBoardMemberMutationResponse>({
        accessToken,
        document: buildRemoveBoardMemberMutation(boardId, userId),
      });

      return response.removeBoardMember;
    },
    onSuccess: async () => {
      await invalidateBoardQueries();
    }
  });

  return {
    activateBoard: async () => {
      return activateBoardMutation.mutateAsync();
    },
    board: boardQuery.data ?? null,
    boardError: boardQuery.error instanceof Error ? boardQuery.error : null,
    changeBoardDescription: async (description: string | null) => {
      return changeBoardDescriptionMutation.mutateAsync(description);
    },
    changeBoardMemberRole: async (userId: string, roleId: string) => {
      return changeBoardMemberRoleMutation.mutateAsync({
        roleId,
        userId
      });
    },
    changeBoardOwnership: async (owner: string) => {
      return changeBoardOwnershipMutation.mutateAsync(owner);
    },
    changeBoardTitle: async (title: string) => {
      return changeBoardTitleMutation.mutateAsync(title);
    },
    deactivateBoard: async () => {
      return deactivateBoardMutation.mutateAsync();
    },
    inviteBoardMember: async (user: string, roleId: string) => {
      return inviteBoardMemberMutation.mutateAsync({
        roleId,
        user
      });
    },
    isInvitingBoardMember: inviteBoardMemberMutation.isPending,
    isLoadingBoard: boardQuery.isLoading,
    isLoadingMembers: membersQuery.isLoading,
    isRemovingBoardMember: removeBoardMemberMutation.isPending,
    isUpdatingBoardDescription: changeBoardDescriptionMutation.isPending,
    isUpdatingBoardMemberRole: changeBoardMemberRoleMutation.isPending,
    isUpdatingBoardOwnership: changeBoardOwnershipMutation.isPending,
    isUpdatingBoardStatus:
      activateBoardMutation.isPending || deactivateBoardMutation.isPending,
    isUpdatingBoardTitle: changeBoardTitleMutation.isPending,
    members: membersQuery.data ?? [],
    membersError: membersQuery.error instanceof Error ? membersQuery.error : null,
    removeBoardMember: async (userId: string) => {
      return removeBoardMemberMutation.mutateAsync(userId);
    }
  };
};
