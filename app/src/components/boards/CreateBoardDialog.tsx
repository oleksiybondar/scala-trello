import type { ReactElement } from "react";

import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";

import { CreateBoardForm } from "@components/boards/CreateBoardForm";
import { useBoards } from "@hooks/useBoards";

interface CreateBoardDialogProps {
  onClose: () => void;
  open: boolean;
}

/**
 * Dialog wrapper for the first-iteration create-board flow.
 */
export const CreateBoardDialog = ({
  onClose,
  open
}: CreateBoardDialogProps): ReactElement => {
  const { createBoard, isCreatingBoard } = useBoards();

  return (
    <Dialog fullWidth maxWidth="md" onClose={onClose} open={open}>
      <DialogTitle>Create board</DialogTitle>
      <CreateBoardForm
        onCancel={onClose}
        onSubmit={async input => {
          await createBoard(input);
          onClose();
        }}
        isSubmitting={isCreatingBoard}
      />
    </Dialog>
  );
};
