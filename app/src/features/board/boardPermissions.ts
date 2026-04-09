import type { Board, BoardPermission } from "@models/board";

export interface BoardPermissionAccess {
  canCreate: boolean;
  canDelete: boolean;
  canModify: boolean;
  canRead: boolean;
  canReassign: boolean;
}

const EMPTY_BOARD_PERMISSION_ACCESS: BoardPermissionAccess = {
  canCreate: false,
  canDelete: false,
  canModify: false,
  canRead: false,
  canReassign: false
};

const getDashboardPermission = (board: Board | null): BoardPermission | null => {
  return (
    board?.currentUserRole?.permissions.find(permission => permission.area === "dashboard") ??
    null
  );
};

export const getBoardPermissionAccess = (board: Board | null): BoardPermissionAccess => {
  const permission = getDashboardPermission(board);

  if (permission === null) {
    return EMPTY_BOARD_PERMISSION_ACCESS;
  }

  return {
    canCreate: permission.canCreate,
    canDelete: permission.canDelete,
    canModify: permission.canModify,
    canRead: permission.canRead,
    canReassign: permission.canReassign
  };
};

export const canManageBoardSettings = (board: Board | null): boolean => {
  const permission = getBoardPermissionAccess(board);

  return (
    permission.canCreate ||
    permission.canModify ||
    permission.canDelete ||
    permission.canReassign
  );
};
