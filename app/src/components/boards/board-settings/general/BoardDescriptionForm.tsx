import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { useBoard } from "@hooks/useBoard";

interface BoardDescriptionFormProps {
  disabled?: boolean;
}

export const BoardDescriptionForm = ({
  disabled = false
}: BoardDescriptionFormProps): ReactElement => {
  const { board, changeBoardDescription, isUpdatingBoardDescription } = useBoard();
  const persistedDescription = board?.description ?? "";
  const [description, setDescription] = useState(persistedDescription);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    setDescription(persistedDescription);
  }, [persistedDescription]);

  const normalizedDescription = description.trim();
  const persistedNormalizedDescription = persistedDescription.trim();
  const hasChanged = normalizedDescription !== persistedNormalizedDescription;
  const isDisabled = disabled || isUpdatingBoardDescription || board === null;

  const handleChange = (
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    setDescription(event.target.value);
  };

  const handleCancel = (): void => {
    setDescription(persistedDescription);
    setErrorMessage(null);
  };

  const handleApply = async (): Promise<void> => {
    if (isDisabled || !hasChanged) {
      return;
    }

    setErrorMessage(null);

    try {
      await changeBoardDescription(
        normalizedDescription.length === 0 ? null : normalizedDescription
      );
    } catch (error: unknown) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Failed to update the board description."
      );
    }
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          {errorMessage !== null ? <Alert severity="error">{errorMessage}</Alert> : null}
          <Stack spacing={1}>
            <Typography variant="h5">Description</Typography>
            <Typography color="textSecondary" variant="body2">
              Update the board summary and working context.
            </Typography>
          </Stack>

          <TextField
            disabled={isDisabled}
            fullWidth
            label="Board description"
            minRows={4}
            multiline
            onChange={handleChange}
            value={description}
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
                disabled={isDisabled}
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
