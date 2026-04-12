import type { ReactElement } from "react";

import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";

import { CreateTicketForm } from "@components/tickets/CreateTicketForm";
import { useTickets } from "@hooks/useTickets";

interface CreateTicketDialogProps {
  onClose: () => void;
  open: boolean;
}

export const CreateTicketDialog = ({
  onClose,
  open
}: CreateTicketDialogProps): ReactElement => {
  const { createTicket, isCreatingTicket } = useTickets();

  return (
    <Dialog fullWidth maxWidth="md" onClose={onClose} open={open}>
      <DialogTitle>Create ticket</DialogTitle>
      <CreateTicketForm
        isSubmitting={isCreatingTicket}
        onCancel={onClose}
        onSubmit={async values => {
          await createTicket({
            ...values,
            assignedToUserId: null
          });
          onClose();
        }}
      />
    </Dialog>
  );
};
