import type { ReactElement } from "react";
import Divider from "@mui/material/Divider";
import Stack from "@mui/material/Stack";

import { BoardDescriptionForm } from "@components/boards/board-settings/general/BoardDescriptionForm";
import { BoardTitleForm } from "@components/boards/board-settings/general/BoardTitleForm";
import { useBoard } from "@hooks/useBoard";

export const BoardGeneralDetailsCard = (): ReactElement => {
  const { isLoadingBoard } = useBoard();

  return (
    <Stack spacing={3}>
      <BoardTitleForm disabled={isLoadingBoard} />
      <Divider />
      <BoardDescriptionForm disabled={isLoadingBoard} />
    </Stack>
  );
};
