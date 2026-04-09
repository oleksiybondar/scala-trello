import type { ReactElement } from "react";
import Divider from "@mui/material/Divider";
import Stack from "@mui/material/Stack";

import { BoardAddMemberCard } from "@components/boards/board-settings/members/BoardAddMemberCard";
import { BoardMembersListCard } from "@components/boards/board-settings/members/BoardMembersListCard";
import { useBoard } from "@hooks/useBoard";

export const BoardMembersManagementCard = (): ReactElement => {
  const { boardPermissionAccess } = useBoard();

  if (
    !boardPermissionAccess.canRead &&
    !boardPermissionAccess.canCreate &&
    !boardPermissionAccess.canModify &&
    !boardPermissionAccess.canDelete
  ) {
    return <></>;
  }

  return (
    <Stack spacing={3}>
      <BoardAddMemberCard />
      <Divider />
      <BoardMembersListCard />
    </Stack>
  );
};
