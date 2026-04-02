import type { ReactElement } from "react";

import Stack from "@mui/material/Stack";

import { ChangeEmailForm } from "@components/forms/user/ChangeEmailForm";
import { ChangePasswordForm } from "@components/forms/user/ChangePasswordForm";
import { ChangeUsernameForm } from "@components/forms/user/ChangeUsernameForm";

export const UserSecuritySettingsPanel = (): ReactElement => {
  return (
    <Stack spacing={3}>
      <ChangeEmailForm />
      <ChangeUsernameForm />
      <ChangePasswordForm />
    </Stack>
  );
};
