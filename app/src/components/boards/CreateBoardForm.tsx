import type { ChangeEvent, ReactElement, SyntheticEvent } from "react";
import { useState } from "react";

import Button from "@mui/material/Button";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import type { CreateBoardInput } from "../../domain/board/graphql";

interface CreateBoardFormProps {
  onCancel: () => void;
  isSubmitting?: boolean | undefined;
  onSubmit: (values: CreateBoardInput) => Promise<void> | void;
}

const INITIAL_FORM_STATE: CreateBoardInput = {
  description: "",
  name: ""
};

/**
 * First-iteration board creation form.
 */
export const CreateBoardForm = ({
  onCancel,
  isSubmitting = false,
  onSubmit
}: CreateBoardFormProps): ReactElement => {
  const [formState, setFormState] = useState(INITIAL_FORM_STATE);

  const isNameValid =
    formState.name.trim().length > 0 && formState.name.trim().length <= 200;

  const handleChange =
    (field: keyof CreateBoardInput) =>
    (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>): void => {
      setFormState(currentState => ({
        ...currentState,
        [field]: event.target.value
      }));
    };

  const handleSubmit = async (
    event: SyntheticEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    if (!isNameValid || isSubmitting) {
      return;
    }

    await Promise.resolve(
      onSubmit({
      description: formState.description?.trim() ?? "",
      name: formState.name.trim()
      })
    );
    setFormState(INITIAL_FORM_STATE);
  };

  return (
    <Stack component="form" onSubmit={handleSubmit} spacing={0}>
      <DialogContent dividers>
        <Stack spacing={3}>
          <Typography color="text.secondary" variant="body2">
            Give the board a clear name. You can optionally add a short
            description for sprint scope or context.
          </Typography>

          <TextField
            autoFocus
            fullWidth
            helperText={`${String(formState.name.length)}/200`}
            label="Board name"
            onChange={handleChange("name")}
            required
            disabled={isSubmitting}
            slotProps={{
              htmlInput: {
                maxLength: 200
              },
              inputLabel: {
                sx: {
                  "&.MuiInputLabel-shrink": {
                    bgcolor: "background.paper",
                    px: 0.5
                  }
                }
              }
            }}
            value={formState.name}
          />

          <TextField
            fullWidth
            label="Description"
            minRows={4}
            multiline
            onChange={handleChange("description")}
            disabled={isSubmitting}
            slotProps={{
              inputLabel: {
                sx: {
                  "&.MuiInputLabel-shrink": {
                    bgcolor: "background.paper",
                    px: 0.5
                  }
                }
              }
            }}
            value={formState.description ?? ""}
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onCancel} variant="outlined">
          Cancel
        </Button>
        <Button disabled={!isNameValid || isSubmitting} type="submit" variant="contained">
          {isSubmitting ? "Creating board..." : "Create board"}
        </Button>
      </DialogActions>
    </Stack>
  );
};
