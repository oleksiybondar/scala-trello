import type { ReactElement } from "react";
import { useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Stack from "@mui/material/Stack";

import { AvatarInput } from "@components/form-elements/avatar/AvatarInput";
import { createAsyncSubmitHandler } from "@helpers/createAsyncActionBuilder";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { FormActionButtons } from "@components/forms/user/FormActionButtons";
import { Card, CardContent } from "@mui/material";
import Typography from "@mui/material/Typography";

interface ChangeAvatarFormProps {
  disabled?: boolean;
}

export const ChangeAvatarForm = ({
  disabled = false
}: ChangeAvatarFormProps): ReactElement => {
  const { currentUser, updateAvatar } = useCurrentUser();
  const persistedAvatarUrl = currentUser?.avatarUrl ?? "";
  const [draftAvatarUrl, setDraftAvatarUrl] = useState(persistedAvatarUrl);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setDraftAvatarUrl(persistedAvatarUrl);
  }, [persistedAvatarUrl]);

  const isChanged = draftAvatarUrl !== persistedAvatarUrl;
  const isDisabled = disabled || isSubmitting;

  const handleCancel = (): void => {
    setDraftAvatarUrl(persistedAvatarUrl);
    setErrorMessage(null);
  };

  const handleApply = createAsyncSubmitHandler()
    .when(() => isChanged && !isDisabled)
    .onStart(() => {
      setErrorMessage(null);
      setIsSubmitting(true);
    })
    .request(() =>
      updateAvatar(draftAvatarUrl.trim().length === 0 ? null : draftAvatarUrl)
    )
    .onError((error: unknown) => {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to update the avatar."
      );
    })
    .onFinally(() => {
      setIsSubmitting(false);
    })
    .handle;

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          {errorMessage !== null ? <Alert severity="error">{errorMessage}</Alert> : null}
        <Stack spacing={1}>
          <Typography variant="h5">Avatar</Typography>
          <Typography color="textSecondary" variant="body2">
            Update your profile picture. Supported image size: up to 1.5 MB.
          </Typography>
        </Stack>
        <AvatarInput
          disabled={isDisabled}
          firstName={currentUser?.firstName}
          lastName={currentUser?.lastName}
          onChange={setDraftAvatarUrl}
          value={draftAvatarUrl}
        />

          {isChanged ? (
            <FormActionButtons
              isDisabled={isDisabled}
              onApply={handleApply}
              onCancel={handleCancel}
            />
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
};
