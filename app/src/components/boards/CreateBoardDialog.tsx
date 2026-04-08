import type { ReactElement } from "react";

import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

interface CreateBoardDialogProps {
  onClose: () => void;
  open: boolean;
}

/**
 * Stub dialog for the future create-board flow.
 *
 * The dialog intentionally reserves a large surface so the real board form can
 * be introduced without reshaping the interaction pattern later.
 */
export const CreateBoardDialog = ({
  onClose,
  open
}: CreateBoardDialogProps): ReactElement => {
  return (
    <Dialog fullWidth maxWidth="xl" onClose={onClose} open={open}>
      <DialogTitle>Create board</DialogTitle>

      <DialogContent dividers>
        <Stack minHeight={420} spacing={2}>
          <Typography variant="body1">
            This dialog is intentionally stubbed for the current iteration.
          </Typography>
          <Typography color="text.secondary" variant="body2">
            The real board creation form will live here once board creation
            fields, validation, and submission flow are implemented.
          </Typography>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} variant="outlined">
          Close
        </Button>
        <Button disabled variant="contained">
          Create board
        </Button>
      </DialogActions>
    </Dialog>
  );
};
