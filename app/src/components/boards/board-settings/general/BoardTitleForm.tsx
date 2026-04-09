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

interface BoardTitleFormProps {
  disabled?: boolean;
}

export const BoardTitleForm = ({
  disabled = false
}: BoardTitleFormProps): ReactElement => {
  const { board, changeBoardTitle, isUpdatingBoardTitle } = useBoard();
  const persistedTitle = board?.name ?? "";
  const [title, setTitle] = useState(persistedTitle);
  const [isTouched, setIsTouched] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    setTitle(persistedTitle);
    setIsTouched(false);
  }, [persistedTitle]);

  const trimmedTitle = title.trim();
  const hasChanged = trimmedTitle !== persistedTitle.trim();
  const hasError = isTouched && trimmedTitle.length === 0;
  const isDisabled = disabled || isUpdatingBoardTitle || board === null;

  const handleChange = (
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    setIsTouched(true);
    setTitle(event.target.value);
  };

  const handleCancel = (): void => {
    setTitle(persistedTitle);
    setIsTouched(false);
    setErrorMessage(null);
  };

  const handleApply = async (): Promise<void> => {
    setIsTouched(true);

    if (isDisabled || trimmedTitle.length === 0 || !hasChanged) {
      return;
    }

    setErrorMessage(null);

    try {
      await changeBoardTitle(trimmedTitle);
    } catch (error: unknown) {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to update the board title."
      );
    }
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          {errorMessage !== null ? <Alert severity="error">{errorMessage}</Alert> : null}
          <Stack spacing={1}>
            <Typography variant="h5">Title</Typography>
            <Typography color="textSecondary" variant="body2">
              Update the board title shown across the workspace.
            </Typography>
          </Stack>

          <TextField
            disabled={isDisabled}
            error={hasError}
            fullWidth
            helperText={hasError ? "Title is required." : " "}
            label="Board title"
            onChange={handleChange}
            required
            value={title}
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
                disabled={isDisabled || trimmedTitle.length === 0}
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
