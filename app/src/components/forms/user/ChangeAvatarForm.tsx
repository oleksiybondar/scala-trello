import type { ReactElement } from "react";
import { useEffect, useState } from "react";

import Button from "@mui/material/Button";
import Stack from "@mui/material/Stack";

import { AvatarInput } from "@components/form-elements/avatar/AvatarInput";
import { useCurrentUser } from "@hooks/useCurrentUser";
import {Card, CardContent } from "@mui/material";
import Typography from "@mui/material/Typography";

interface ChangeAvatarFormProps {
  disabled?: boolean;
  onSubmit?: (avatarUrl: string) => Promise<void> | void;
}

export const ChangeAvatarForm = ({
  disabled = false,
  onSubmit
}: ChangeAvatarFormProps): ReactElement => {
  const { currentUser } = useCurrentUser();
  const persistedAvatarUrl = currentUser?.avatarUrl ?? "";
  const [draftAvatarUrl, setDraftAvatarUrl] = useState(persistedAvatarUrl);

  useEffect(() => {
    setDraftAvatarUrl(persistedAvatarUrl);
  }, [persistedAvatarUrl]);

  const isChanged = draftAvatarUrl !== persistedAvatarUrl;

  const handleCancel = (): void => {
    setDraftAvatarUrl(persistedAvatarUrl);
  };

  const handleApply = async (): Promise<void> => {
    if (!isChanged || disabled) {
      return;
    }

    await onSubmit?.(draftAvatarUrl);
  };

  return (
      <Card variant="outlined">
        <CardContent>
      <Stack padding={3} spacing={3} >
        <Stack spacing={1}>
          <Typography variant="h5">Avatar</Typography>
          <Typography color="textSecondary" variant="body2">
            Update your profile picture. Supported image size: up to 1.5 MB.
          </Typography>
        </Stack>
        <AvatarInput
          disabled={disabled}
          onChange={setDraftAvatarUrl}
          value={draftAvatarUrl}
        />

        { isChanged && ( <Stack direction={{ xs: "column-reverse", sm: "row" }} spacing={1.5} justifyContent="flex-end">
          <Button
            disabled={disabled}
            onClick={handleCancel}
            variant="outlined"
          >
            Cancel
          </Button>
          <Button
            disabled={disabled}
            onClick={() => {
              void handleApply();
            }}
            variant="contained"
          >
            Apply
          </Button>
        </Stack> ) }
      </Stack>
        </CardContent>
      </Card>
  );
};
