import type { PropsWithChildren, ReactElement } from "react";
import { useParams } from "react-router-dom";

import { BoardContext } from "@contexts/board-context";
import {
  canManageBoardSettings,
  getBoardPermissionAccess
} from "../domain/board/boardPermissions";
import { useBoardService } from "../domain/board/useBoardService";

export const BoardProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const { boardId = "" } = useParams();
  const {
    activateBoard,
    board,
    boardError,
    changeBoardDescription,
    changeBoardMemberRole,
    changeBoardOwnership,
    changeBoardTitle,
    deactivateBoard,
    inviteBoardMember,
    isInvitingBoardMember,
    isLoadingBoard,
    isLoadingMembers,
    isRemovingBoardMember,
    isUpdatingBoardDescription,
    isUpdatingBoardMemberRole,
    isUpdatingBoardOwnership,
    isUpdatingBoardStatus,
    isUpdatingBoardTitle,
    members,
    membersError,
    removeBoardMember
  } = useBoardService({
    boardId,
  });
  const boardPermissionAccess = getBoardPermissionAccess(board);

  return (
    <BoardContext.Provider
      value={{
        activateBoard,
        board,
        boardError,
        boardPermissionAccess,
        canManageBoardSettings: canManageBoardSettings(board),
        changeBoardDescription,
        changeBoardMemberRole,
        changeBoardOwnership,
        changeBoardTitle,
        deactivateBoard,
        inviteBoardMember,
        isInvitingBoardMember,
        isLoadingBoard,
        isLoadingMembers,
        isRemovingBoardMember,
        isUpdatingBoardDescription,
        isUpdatingBoardMemberRole,
        isUpdatingBoardOwnership,
        isUpdatingBoardStatus,
        isUpdatingBoardTitle,
        members,
        membersError,
        removeBoardMember
      }}
    >
      {children}
    </BoardContext.Provider>
  );
};
