import type { ReactElement } from "react";

import Divider from "@mui/material/Divider";
import Stack from "@mui/material/Stack";

import { ChangeAvatarForm } from "@components/forms/user/ChangeAvatarForm";
import { UserProfileForm } from "@components/forms/user/UserProfileForm";

export const UserProfileSettingsPanel = (): ReactElement => {
  return (
<>
        <Stack spacing={3}>
            <ChangeAvatarForm />
          <Divider />
            <UserProfileForm />
          </Stack>
</>
  );
};
