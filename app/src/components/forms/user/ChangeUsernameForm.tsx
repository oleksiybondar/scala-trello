import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { useCurrentUser } from "@hooks/useCurrentUser";

interface ChangeUsernameFormProps {
  disabled?: boolean;
  onSubmit?: (payload: { username: string }) => Promise<void> | void;
}

export const ChangeUsernameForm = ({
  disabled = false,
  onSubmit
}: ChangeUsernameFormProps): ReactElement => {
  const { currentUser } = useCurrentUser();
  const persistedUsername = currentUser?.username ?? "";
  const [username, setUsername] = useState(persistedUsername);
  const [isTouched, setIsTouched] = useState(false);

  useEffect(() => {
    setUsername(persistedUsername);
    setIsTouched(false);
  }, [persistedUsername]);

  const trimmedUsername = username.trim();
  const isEmpty = trimmedUsername.length === 0;
  const hasChanged = trimmedUsername !== persistedUsername.trim();
  const hasError = isTouched && isEmpty;

  const handleChange = (
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    setIsTouched(true);
    setUsername(event.target.value);
  };

  const handleCancel = (): void => {
    setUsername(persistedUsername);
    setIsTouched(false);
  };

  const handleApply = async (): Promise<void> => {
    setIsTouched(true);

    if (disabled || isEmpty || !hasChanged) {
      return;
    }

    await onSubmit?.({
      username: trimmedUsername
    });
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          <Stack spacing={1}>
            <Typography variant="h5">Username</Typography>
            <Typography color="textSecondary" variant="body2">
              Update the username used to identify your account.
            </Typography>
          </Stack>

          <TextField
            disabled={disabled}
            error={hasError}
            fullWidth
            helperText={hasError ? "Username is required." : " "}
            label="Username"
            onChange={handleChange}
            required
            value={username}
          />

          {hasChanged ? (
            <Stack
              direction={{ xs: "column-reverse", sm: "row" }}
              justifyContent="flex-end"
              spacing={1.5}
            >
              <Button disabled={disabled} onClick={handleCancel} variant="outlined">
                Cancel
              </Button>
              <Button
                disabled={disabled || isEmpty}
                onClick={() => {
                  void handleApply();
                }}
                variant="contained"
              >
                Apply
              </Button>
            </Stack>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
};
