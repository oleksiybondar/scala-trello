import type { PropsWithChildren, ReactElement } from "react";
import { useParams } from "react-router-dom";

import { BoardContext } from "@contexts/board-context";
import {
  canManageBoardSettings,
  getBoardPermissionAccess
} from "@features/board/boardPermissions";
import { useBoardMetadataMutation } from "@features/board/useBoardMetadataMutation";
import { useBoardQuery } from "@features/board/useBoardQuery";

export const BoardProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const { boardId = "" } = useParams();
  const boardQuery = useBoardQuery(boardId);
  const {
    activateBoardMutation,
    changeBoardDescriptionMutation,
    changeBoardOwnershipMutation,
    changeBoardTitleMutation,
    deactivateBoardMutation
  } = useBoardMetadataMutation();
  const board = boardQuery.data ?? null;
  const boardPermissionAccess = getBoardPermissionAccess(board);

  return (
    <BoardContext.Provider
      value={{
        activateBoard: async () => {
          await activateBoardMutation.mutateAsync({
            boardId
          });
        },
        board,
        boardError: boardQuery.error instanceof Error ? boardQuery.error : null,
        boardPermissionAccess,
        canManageBoardSettings: canManageBoardSettings(board),
        changeBoardDescription: async (description: string | null) => {
          await changeBoardDescriptionMutation.mutateAsync({
            boardId,
            description
          });
        },
        changeBoardOwnership: async (owner: string) => {
          await changeBoardOwnershipMutation.mutateAsync({
            boardId,
            owner
          });
        },
        changeBoardTitle: async (title: string) => {
          await changeBoardTitleMutation.mutateAsync({
            boardId,
            title
          });
        },
        deactivateBoard: async () => {
          await deactivateBoardMutation.mutateAsync({
            boardId
          });
        },
        isLoadingBoard: boardQuery.isLoading,
        isUpdatingBoardDescription: changeBoardDescriptionMutation.isPending,
        isUpdatingBoardOwnership: changeBoardOwnershipMutation.isPending,
        isUpdatingBoardStatus:
          activateBoardMutation.isPending || deactivateBoardMutation.isPending,
        isUpdatingBoardTitle: changeBoardTitleMutation.isPending
      }}
    >
      {children}
    </BoardContext.Provider>
  );
};
