import type { ReactElement } from "react";
import Divider from "@mui/material/Divider";
import Stack from "@mui/material/Stack";

import { BoardChangeOwnerForm } from "@components/boards/board-settings/ownership/BoardChangeOwnerForm";
import { BoardStatusForm } from "@components/boards/board-settings/ownership/BoardStatusForm";
import { useBoard } from "@hooks/useBoard";

export const BoardOwnershipManagementCard = (): ReactElement => {
  const { boardPermissionAccess, isLoadingBoard } = useBoard();

  if (
    !boardPermissionAccess.canModify &&
    !boardPermissionAccess.canDelete &&
    !boardPermissionAccess.canReassign
  ) {
    return <></>;
  }

  return (
    <Stack spacing={3}>
      {boardPermissionAccess.canModify || boardPermissionAccess.canReassign ? (
        <>
          <BoardChangeOwnerForm disabled={isLoadingBoard} />
          {boardPermissionAccess.canDelete ? <Divider /> : null}
        </>
      ) : null}
      {boardPermissionAccess.canDelete ? <BoardStatusForm disabled={isLoadingBoard} /> : null}
    </Stack>
  );
};
