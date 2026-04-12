import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { FormActionButtons } from "@components/forms/user/FormActionButtons";
import { createAsyncSubmitHandler } from "@helpers/createAsyncActionBuilder";
import { useCurrentUser } from "@hooks/useCurrentUser";

interface ChangeUsernameFormProps {
  disabled?: boolean;
}

export const ChangeUsernameForm = ({
  disabled = false
}: ChangeUsernameFormProps): ReactElement => {
  const { changeUsername, currentUser } = useCurrentUser();
  const persistedUsername = currentUser?.username ?? "";
  const [username, setUsername] = useState(persistedUsername);
  const [isTouched, setIsTouched] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setUsername(persistedUsername);
    setIsTouched(false);
  }, [persistedUsername]);

  const trimmedUsername = username.trim();
  const isEmpty = trimmedUsername.length === 0;
  const hasChanged = trimmedUsername !== persistedUsername.trim();
  const hasError = isTouched && isEmpty;
  const isDisabled = disabled || isSubmitting;

  const handleChange = (
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    setIsTouched(true);
    setUsername(event.target.value);
  };

  const handleCancel = (): void => {
    setUsername(persistedUsername);
    setIsTouched(false);
    setErrorMessage(null);
  };

  const handleApply = createAsyncSubmitHandler()
    .when(() => {
      setIsTouched(true);

      return !isDisabled && !isEmpty && hasChanged;
    })
    .onStart(() => {
      setErrorMessage(null);
      setIsSubmitting(true);
    })
    .request(() => changeUsername(trimmedUsername))
    .onSuccess(() => {
      setIsTouched(false);
    })
    .onError((error: unknown) => {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to update the username."
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
            <Typography variant="h5">Username</Typography>
            <Typography color="textSecondary" variant="body2">
              Update the username used to identify your account.
            </Typography>
          </Stack>

          <TextField
            disabled={isDisabled}
            error={hasError}
            fullWidth
            helperText={hasError ? "Username is required." : " "}
            label="Username"
            onChange={handleChange}
            required
            value={username}
          />

          {hasChanged ? (
            <FormActionButtons
              applyDisabled={isEmpty}
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
