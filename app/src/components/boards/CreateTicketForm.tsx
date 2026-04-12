import type { ChangeEvent, ReactElement, SyntheticEvent } from "react";
import { useState } from "react";

import Button from "@mui/material/Button";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

export interface CreateTicketInput {
  acceptanceCriteria: string;
  description: string;
  estimatedMinutes: string;
  title: string;
}

interface CreateTicketFormProps {
  isSubmitting?: boolean | undefined;
  onCancel: () => void;
  onSubmit: (values: CreateTicketInput) => Promise<void> | void;
}

const INITIAL_FORM_STATE: CreateTicketInput = {
  acceptanceCriteria: "",
  description: "",
  estimatedMinutes: "",
  title: ""
};

export const CreateTicketForm = ({
  isSubmitting = false,
  onCancel,
  onSubmit
}: CreateTicketFormProps): ReactElement => {
  const [formState, setFormState] = useState(INITIAL_FORM_STATE);

  const isTitleValid =
    formState.title.trim().length > 0 && formState.title.trim().length <= 200;

  const handleChange =
    (field: keyof CreateTicketInput) =>
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

    if (!isTitleValid || isSubmitting) {
      return;
    }

    await Promise.resolve(
      onSubmit({
        acceptanceCriteria: formState.acceptanceCriteria.trim(),
        description: formState.description.trim(),
        estimatedMinutes: formState.estimatedMinutes.trim(),
        title: formState.title.trim()
      })
    );

    setFormState(INITIAL_FORM_STATE);
  };

  return (
    <Stack component="form" onSubmit={handleSubmit} spacing={0}>
      <DialogContent dividers>
        <Stack spacing={3}>
          <Typography color="text.secondary" variant="body2">
            Draft the ticket details here. Submission wiring comes next once the
            ticket mutation flow is connected.
          </Typography>

          <TextField
            autoFocus
            disabled={isSubmitting}
            fullWidth
            helperText={`${String(formState.title.length)}/200`}
            label="Ticket title"
            onChange={handleChange("title")}
            required
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
            value={formState.title}
          />

          <TextField
            disabled={isSubmitting}
            fullWidth
            label="Description"
            minRows={4}
            multiline
            onChange={handleChange("description")}
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
            value={formState.description}
          />

          <TextField
            disabled={isSubmitting}
            fullWidth
            label="Acceptance criteria"
            minRows={3}
            multiline
            onChange={handleChange("acceptanceCriteria")}
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
            value={formState.acceptanceCriteria}
          />

          <TextField
            disabled={isSubmitting}
            fullWidth
            label="Estimated minutes"
            onChange={handleChange("estimatedMinutes")}
            slotProps={{
              htmlInput: {
                inputMode: "numeric",
                pattern: "[0-9]*"
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
            value={formState.estimatedMinutes}
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onCancel} variant="outlined">
          Cancel
        </Button>
        <Button disabled={!isTitleValid || isSubmitting} type="submit" variant="contained">
          {isSubmitting ? "Creating ticket..." : "Create ticket"}
        </Button>
      </DialogActions>
    </Stack>
  );
};
