import type { ReactElement } from "react";
import Divider from "@mui/material/Divider";
import Stack from "@mui/material/Stack";

import { BoardMembersListCard } from "@components/boards/board-settings/members/BoardMembersListCard";
import { BoardSettingsPlaceholderPanel } from "@components/boards/board-settings/BoardSettingsPlaceholderPanel";

export const BoardMembersManagementCard = (): ReactElement => {
  return (
    <Stack spacing={3}>
      <BoardSettingsPlaceholderPanel
        description="Future section for searching users and inviting a new member."
        helperText="User query input and invite action scaffold"
        title="Add New Member"
      />
      <Divider />
      <BoardMembersListCard />
    </Stack>
  );
};
