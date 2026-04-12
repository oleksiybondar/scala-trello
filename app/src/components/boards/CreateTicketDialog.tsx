import type { ReactElement } from "react";

import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";

import { CreateTicketForm } from "@components/boards/CreateTicketForm";

interface CreateTicketDialogProps {
  onClose: () => void;
  open: boolean;
}

export const CreateTicketDialog = ({
  onClose,
  open
}: CreateTicketDialogProps): ReactElement => {
  return (
    <Dialog fullWidth maxWidth="md" onClose={onClose} open={open}>
      <DialogTitle>Create ticket</DialogTitle>
      <CreateTicketForm
        onCancel={onClose}
        onSubmit={() => {
          onClose();
        }}
      />
    </Dialog>
  );
};
