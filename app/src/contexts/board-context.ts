import { createContext } from "react";

import type { Board, BoardMember } from "../domain/board/graphql";
import type { BoardPermissionAccess } from "../domain/board/boardPermissions";

export interface BoardContextValue {
  activateBoard: () => Promise<Board>;
  board: Board | null;
  boardError: Error | null;
  boardPermissionAccess: BoardPermissionAccess;
  canManageBoardSettings: boolean;
  changeBoardDescription: (description: string | null) => Promise<Board>;
  changeBoardMemberRole: (userId: string, roleId: string) => Promise<BoardMember>;
  changeBoardOwnership: (owner: string) => Promise<Board>;
  changeBoardTitle: (title: string) => Promise<Board>;
  deactivateBoard: () => Promise<Board>;
  inviteBoardMember: (user: string, roleId: string) => Promise<BoardMember>;
  isLoadingBoard: boolean;
  isLoadingMembers: boolean;
  isInvitingBoardMember: boolean;
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

const missingBoardProvider = (): never => {
  throw new Error("BoardContext is missing its provider.");
};

export const BoardContext = createContext<BoardContextValue>({
  activateBoard: missingBoardProvider,
  board: null,
  boardError: null,
  boardPermissionAccess: {
    canCreate: false,
    canDelete: false,
    canModify: false,
    canRead: false,
    canReassign: false
  },
  canManageBoardSettings: false,
  changeBoardDescription: missingBoardProvider,
  changeBoardMemberRole: missingBoardProvider,
  changeBoardOwnership: missingBoardProvider,
  changeBoardTitle: missingBoardProvider,
  deactivateBoard: missingBoardProvider,
  isLoadingBoard: false,
  isLoadingMembers: false,
  isInvitingBoardMember: false,
  isRemovingBoardMember: false,
  isUpdatingBoardDescription: false,
  isUpdatingBoardMemberRole: false,
  isUpdatingBoardOwnership: false,
  isUpdatingBoardStatus: false,
  isUpdatingBoardTitle: false,
  inviteBoardMember: missingBoardProvider,
  members: [],
  membersError: null,
  removeBoardMember: missingBoardProvider
});
