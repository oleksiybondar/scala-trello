import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { Person } from "@components/avatar/Person";
import { useBoard } from "@hooks/useBoard";

interface BoardChangeOwnerFormProps {
  disabled?: boolean;
}

export const BoardChangeOwnerForm = ({
  disabled = false
}: BoardChangeOwnerFormProps): ReactElement => {
  const { board, changeBoardOwnership, isUpdatingBoardOwnership } = useBoard();
  const [owner, setOwner] = useState("");
  const [isTouched, setIsTouched] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    setOwner("");
    setIsTouched(false);
  }, [board?.ownerUserId]);

  const trimmedOwner = owner.trim();
  const hasChanged = trimmedOwner.length > 0;
  const hasError = isTouched && trimmedOwner.length === 0;
  const isDisabled = disabled || isUpdatingBoardOwnership || board === null;

  const handleChange = (
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    setIsTouched(true);
    setOwner(event.target.value);
  };

  const handleCancel = (): void => {
    setOwner("");
    setIsTouched(false);
    setErrorMessage(null);
  };

  const handleApply = async (): Promise<void> => {
    setIsTouched(true);

    if (isDisabled || trimmedOwner.length === 0 || !hasChanged) {
      return;
    }

    setErrorMessage(null);

    try {
      await changeBoardOwnership(trimmedOwner);
      setOwner("");
      setIsTouched(false);
    } catch (error: unknown) {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to change the board owner."
      );
    }
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          {errorMessage !== null ? <Alert severity="error">{errorMessage}</Alert> : null}
          <Stack spacing={1}>
            <Typography variant="h5">Change owner</Typography>
            <Typography color="textSecondary" variant="body2">
              Reassign this board to another user by username or email.
            </Typography>
          </Stack>

          <Stack spacing={0.5}>
            <Typography color="textSecondary" variant="body2">
              Current owner
            </Typography>
            <Person fallbackLabel="Unknown owner" person={board?.owner ?? null} />
          </Stack>

          <TextField
            disabled={isDisabled}
            error={hasError}
            fullWidth
            helperText={hasError ? "Username or email is required." : " "}
            label="New owner username or email"
            onChange={handleChange}
            required
            value={owner}
          />

          {hasChanged ? (
            <Stack
              direction={{ xs: "column-reverse", sm: "row" }}
              justifyContent="flex-end"
              spacing={1.5}
            >
              <Button disabled={isDisabled} onClick={handleCancel} variant="outlined">
                Cancel
              </Button>
              <Button
                disabled={isDisabled || trimmedOwner.length === 0}
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
