import type { ReactElement } from "react";
import { useEffect } from "react";

import { BoardSettingsLayout } from "@components/boards/board-settings/BoardSettingsLayout";
import { useDictionaries } from "@hooks/useDictionaries";
import { BoardProvider } from "@providers/BoardProvider";

const BoardSettingsPageBody = (): ReactElement => {
  const { loadDictionaries } = useDictionaries();

  useEffect(() => {
    void loadDictionaries();
  }, [loadDictionaries]);

  return <BoardSettingsLayout />;
};

export const BoardSettingsPage = (): ReactElement => {
  return (
    <BoardProvider>
      <BoardSettingsPageBody />
    </BoardProvider>
  );
};
