import type { ReactElement } from "react";
import Divider from "@mui/material/Divider";
import Stack from "@mui/material/Stack";

import { BoardChangeOwnerForm } from "@components/boards/board-settings/ownership/BoardChangeOwnerForm";
import { BoardStatusForm } from "@components/boards/board-settings/ownership/BoardStatusForm";
import { useBoard } from "@hooks/useBoard";

export const BoardOwnershipManagementCard = (): ReactElement => {
  const { isLoadingBoard } = useBoard();

  return (
    <Stack spacing={3}>
      <BoardChangeOwnerForm disabled={isLoadingBoard} />
      <Divider />
      <BoardStatusForm disabled={isLoadingBoard} />
    </Stack>
  );
};
