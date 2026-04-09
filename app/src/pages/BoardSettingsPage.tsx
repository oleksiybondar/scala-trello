import type { ReactElement } from "react";

import { BoardSettingsLayout } from "@components/boards/board-settings/BoardSettingsLayout";
import { BoardProvider } from "@providers/BoardProvider";

export const BoardSettingsPage = (): ReactElement => {
  return (
    <BoardProvider>
      <BoardSettingsLayout />
    </BoardProvider>
  );
};
