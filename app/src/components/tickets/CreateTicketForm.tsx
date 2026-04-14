import type { ChangeEvent, ReactElement, SyntheticEvent } from "react";
import { useState } from "react";

import Button from "@mui/material/Button";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";

import { TicketPrioritySelect } from "@components/tickets/TicketPrioritySelect";
import { TicketSeveritySelect } from "@components/tickets/TicketSeveritySelect";
import { TimeInput } from "@components/time-tracking/TimeInput";
import type { DictionarySeverity } from "../../domain/dictionaries/graphql";

export interface CreateTicketInput {
  acceptanceCriteria: string;
  description: string;
  estimatedMinutes: number | null;
  priority: number;
  severityId: string | null;
  title: string;
}

interface CreateTicketFormProps {
  initialPriority?: number | undefined;
  initialSeverityId?: string | null | undefined;
  isSubmitting?: boolean | undefined;
  onCancel: () => void;
  onSubmit: (values: CreateTicketInput) => Promise<void> | void;
  severities: DictionarySeverity[];
}

const INITIAL_FORM_STATE: CreateTicketInput = {
  acceptanceCriteria: "",
  description: "",
  estimatedMinutes: null,
  priority: 5,
  severityId: null,
  title: ""
};

export const CreateTicketForm = ({
  initialPriority = INITIAL_FORM_STATE.priority,
  initialSeverityId = INITIAL_FORM_STATE.severityId,
  isSubmitting = false,
  onCancel,
  onSubmit,
  severities
}: CreateTicketFormProps): ReactElement => {
  const [formState, setFormState] = useState({
    ...INITIAL_FORM_STATE,
    priority: initialPriority,
    severityId: initialSeverityId
  });

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
        estimatedMinutes: formState.estimatedMinutes,
        priority: formState.priority,
        severityId: formState.severityId,
        title: formState.title.trim()
      })
    );

    setFormState({
      ...INITIAL_FORM_STATE,
      priority: initialPriority,
      severityId: initialSeverityId
    });
  };

  return (
    <Stack component="form" onSubmit={handleSubmit} spacing={0}>
      <DialogContent dividers>
        <Stack spacing={3}>
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

          <Stack direction={{ md: "row", xs: "column" }} spacing={2}>
            <TicketSeveritySelect
                disabled={isSubmitting}
                onChange={severityId => {
                  setFormState(currentState => ({
                    ...currentState,
                    severityId
                  }));
                }}
                severities={severities}
                value={formState.severityId}
            />

            <TicketPrioritySelect
                disabled={isSubmitting}
                onChange={priority => {
                  setFormState(currentState => ({
                    ...currentState,
                    priority
                  }));
                }}
                value={formState.priority}
            />
          </Stack>

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

          <TimeInput
            disabled={isSubmitting}
            label="Estimate (HH:MM)"
            onChange={value => {
              setFormState(currentState => ({
                ...currentState,
                estimatedMinutes: value
              }));
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
