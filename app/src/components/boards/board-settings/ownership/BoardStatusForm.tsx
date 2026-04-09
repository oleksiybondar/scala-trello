import type { ReactElement } from "react";
import { useState } from "react";

import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { useBoard } from "@hooks/useBoard";

interface BoardStatusFormProps {
  disabled?: boolean;
}

export const BoardStatusForm = ({
  disabled = false
}: BoardStatusFormProps): ReactElement => {
  const { activateBoard, board, deactivateBoard, isUpdatingBoardStatus } = useBoard();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const isDisabled = disabled || isUpdatingBoardStatus || board === null;
  const isActive = board?.active ?? false;

  const handleDeactivate = async (): Promise<void> => {
    if (isDisabled || !isActive) {
      return;
    }

    setErrorMessage(null);

    try {
      await deactivateBoard();
    } catch (error: unknown) {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to deactivate the board."
      );
    }
  };

  const handleActivate = async (): Promise<void> => {
    if (isDisabled || isActive) {
      return;
    }

    setErrorMessage(null);

    try {
      await activateBoard();
    } catch (error: unknown) {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to activate the board."
      );
    }
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          {errorMessage !== null ? <Alert severity="error">{errorMessage}</Alert> : null}
          <Stack spacing={1}>
            <Typography variant="h5">Deactivate / Reactivate</Typography>
            <Typography color="textSecondary" variant="body2">
              Manage whether this board stays active for ongoing work.
            </Typography>
          </Stack>

          <Stack
            alignItems={{ sm: "center" }}
            direction={{ xs: "column", sm: "row" }}
            justifyContent="space-between"
            spacing={2}
          >
            <Stack spacing={1}>
              <Stack alignItems="center" direction="row" spacing={1}>
                <Typography variant="body2">Current status</Typography>
                <Chip
                  color={isActive ? "success" : "default"}
                  label={isActive ? "Active" : "Closed"}
                  size="small"
                />
              </Stack>
              <Typography color="textSecondary" variant="body2">
                Switch the board between active and closed states.
              </Typography>
            </Stack>

            {isActive ? (
              <Button
                color="error"
                disabled={isDisabled}
                onClick={() => {
                  void handleDeactivate();
                }}
                variant="contained"
              >
                Deactivate board
              </Button>
            ) : (
              <Button
                disabled={isDisabled}
                onClick={() => {
                  void handleActivate();
                }}
                variant="contained"
              >
                Activate board
              </Button>
            )}
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  );
};
