import type { ReactElement } from "react";

import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Typography from "@mui/material/Typography";

interface TimeTrackingModalProps {
  isOpen: boolean;
  onClose: () => void;
  ticketId: string | null;
}

export const TimeTrackingModal = ({
  isOpen,
  onClose,
  ticketId
}: TimeTrackingModalProps): ReactElement => {
  return (
    <Dialog fullWidth maxWidth="sm" onClose={onClose} open={isOpen}>
      <DialogTitle>Log time</DialogTitle>
      <DialogContent dividers>
        <Typography color="text.secondary" variant="body2">
          Time tracking modal stub.
        </Typography>
        <Typography sx={{ mt: 1 }} variant="body2">
          {ticketId === null ? "No ticket selected." : `Ticket id: ${ticketId}`}
        </Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
};
