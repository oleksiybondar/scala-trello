import type { ReactElement } from "react";
import { useEffect } from "react";

import Alert from "@mui/material/Alert";
import Dialog from "@mui/material/Dialog";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Typography from "@mui/material/Typography";

import { CreateTicketForm } from "@components/tickets/CreateTicketForm";
import { useSeverities } from "@hooks/useSeverities";
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
  const {
    hasLoadedSeverities,
    isLoadingSeverities,
    loadSeverities,
    severities,
    severitiesError
  } = useSeverities();

  useEffect(() => {
    if (!open || hasLoadedSeverities || isLoadingSeverities) {
      return;
    }

    void loadSeverities();
  }, [hasLoadedSeverities, isLoadingSeverities, loadSeverities, open]);

  const defaultSeverityId =
    severities.find(severity => severity.name.trim().toLowerCase() === "normal")?.severityId ??
    severities[0]?.severityId ??
    null;

  return (
    <Dialog fullWidth maxWidth="md" onClose={onClose} open={open}>
      <DialogTitle>Create ticket</DialogTitle>
      {severitiesError !== null ? (
        <DialogContent dividers>
          <Alert severity="error">{severitiesError.message}</Alert>
        </DialogContent>
      ) : hasLoadedSeverities && severities.length > 0 ? (
        <CreateTicketForm
          initialPriority={5}
          initialSeverityId={defaultSeverityId}
          isSubmitting={isCreatingTicket}
          onCancel={onClose}
          onSubmit={async values => {
            await createTicket({
              ...values,
              assignedToUserId: null
            });
            onClose();
          }}
          severities={severities}
        />
      ) : (
        <DialogContent dividers>
          <Typography color="text.secondary" variant="body2">
            {isLoadingSeverities ? "Loading severities..." : "Preparing ticket dictionaries..."}
          </Typography>
        </DialogContent>
      )}
    </Dialog>
  );
};
