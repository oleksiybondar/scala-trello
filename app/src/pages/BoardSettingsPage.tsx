import type { ReactElement } from "react";

import { BoardSettingsLayout } from "@components/boards/board-settings/BoardSettingsLayout";
import { BoardProvider } from "@providers/BoardProvider";
import { RolesProvider } from "@providers/RolesProvider";

export const BoardSettingsPage = (): ReactElement => {
  return (
    <BoardProvider>
      <RolesProvider>
        <BoardSettingsLayout />
      </RolesProvider>
    </BoardProvider>
  );
};
