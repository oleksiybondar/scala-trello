import type { ChangeEvent, ReactElement, SyntheticEvent } from "react";
import { useEffect, useMemo, useState } from "react";

import Button from "@mui/material/Button";
import Link from "@mui/material/Link";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { Link as RouterLink } from "react-router-dom";

import { TimeInput } from "@components/time-tracking/TimeInput";
import { TimeTrackingActivitySelect } from "@components/time-tracking/TimeTrackingActivitySelect";
import type { DictionaryTimeTrackingActivity } from "../../../domain/dictionaries/graphql";
import type { UpdateTimeTrackingEntryInput } from "../../../domain/time-tracking/useTimeTrackingEntryService";
import type { TimeTrackingEntry } from "../../../domain/time-tracking/graphql";

interface MyTimeEntryFormProps {
  activities: DictionaryTimeTrackingActivity[];
  entry: TimeTrackingEntry;
  isSubmitting: boolean;
  onCancel: () => void;
  onSubmit: (input: UpdateTimeTrackingEntryInput) => Promise<void>;
}

const padTimeSegment = (value: number): string => {
  return String(value).padStart(2, "0");
};

const toDateTimeLocalValue = (value: Date): string => {
  return [
    String(value.getFullYear()),
    padTimeSegment(value.getMonth() + 1),
    padTimeSegment(value.getDate())
  ].join("-")
    + "T"
    + [padTimeSegment(value.getHours()), padTimeSegment(value.getMinutes())].join(":");
};

const parseDateTimeLocalToIso = (value: string): string | null => {
  const parsedDate = new Date(value);

  return Number.isNaN(parsedDate.getTime()) ? null : parsedDate.toISOString();
};

const toSafeDateTimeLocalValue = (value: string): string => {
  const parsedDate = new Date(value);

  if (Number.isNaN(parsedDate.getTime())) {
    return toDateTimeLocalValue(new Date());
  }

  return toDateTimeLocalValue(parsedDate);
};

export const MyTimeEntryForm = ({
  activities,
  entry,
  isSubmitting,
  onCancel,
  onSubmit
}: MyTimeEntryFormProps): ReactElement => {
  const [selectedActivityId, setSelectedActivityId] = useState(entry.activityId);
  const [durationMinutes, setDurationMinutes] = useState<number | null>(entry.durationMinutes);
  const [loggedAt, setLoggedAt] = useState(toSafeDateTimeLocalValue(entry.loggedAt));
  const [description, setDescription] = useState(entry.description ?? "");
  const [submitError, setSubmitError] = useState<string | null>(null);

  useEffect(() => {
    setSelectedActivityId(entry.activityId);
    setDurationMinutes(entry.durationMinutes);
    setLoggedAt(toSafeDateTimeLocalValue(entry.loggedAt));
    setDescription(entry.description ?? "");
    setSubmitError(null);
  }, [entry]);

  const parsedActivityId = useMemo(() => {
    const parsed = Number.parseInt(selectedActivityId, 10);

    return Number.isNaN(parsed) ? null : parsed;
  }, [selectedActivityId]);
  const loggedAtIso = parseDateTimeLocalToIso(loggedAt);
  const canSubmit =
    !isSubmitting
    && parsedActivityId !== null
    && durationMinutes !== null
    && durationMinutes > 0
    && loggedAtIso !== null;

  const handleSubmit = async (event: SyntheticEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();

    if (!canSubmit) {
      return;
    }

    setSubmitError(null);

    try {
      await onSubmit({
        activityId: parsedActivityId,
        description,
        durationMinutes,
        entryId: entry.entryId,
        loggedAt: loggedAtIso
      });
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Failed to update time entry.");
    }
  };

  return (
    <Paper component="form" onSubmit={handleSubmit} sx={{ p: 2 }} variant="outlined">
      <Stack spacing={1.5}>
        <Stack direction="row" spacing={1}>
          <Link
            component={RouterLink}
            to={`/tickets/${entry.ticketId}`}
            underline="hover"
            variant="body2"
          >
            {entry.ticket?.title ?? `Ticket #${entry.ticketId}`}
          </Link>
          {entry.ticket?.board !== null && entry.ticket?.board !== undefined ? (
            <Link
              color="text.secondary"
              component={RouterLink}
              to={`/boards/${entry.ticket.board.boardId}`}
              underline="hover"
              variant="body2"
            >
              {entry.ticket.board.title}
            </Link>
          ) : null}
        </Stack>

        <TimeTrackingActivitySelect
          activities={activities}
          disabled={isSubmitting}
          onChange={nextValue => {
            setSelectedActivityId(typeof nextValue === "string" ? nextValue : (nextValue[0] ?? ""));
          }}
          value={selectedActivityId}
        />

        <TimeInput
          disabled={isSubmitting}
          label="Duration (HH:MM)"
          onChange={setDurationMinutes}
          value={durationMinutes}
        />

        <TextField
          disabled={isSubmitting}
          error={loggedAtIso === null}
          fullWidth
          helperText={loggedAtIso === null ? "Invalid date/time value." : "When the work happened."}
          label="Logged at"
          onChange={(event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
            setLoggedAt(event.target.value);
          }}
          slotProps={{
            inputLabel: {
              shrink: true,
              sx: {
                "&.MuiInputLabel-shrink": {
                  bgcolor: "background.paper",
                  px: 0.5
                }
              }
            }
          }}
          type="datetime-local"
          value={loggedAt}
        />

        <TextField
          disabled={isSubmitting}
          fullWidth
          label="Description"
          minRows={2}
          multiline
          onChange={(event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
            setDescription(event.target.value);
          }}
          value={description}
        />

        {submitError !== null ? (
          <Typography color="error" variant="body2">
            {submitError}
          </Typography>
        ) : null}

        <Stack direction="row" justifyContent="flex-end" spacing={1}>
          <Button disabled={isSubmitting} onClick={onCancel} variant="outlined">
            Cancel
          </Button>
          <Button disabled={!canSubmit} type="submit" variant="contained">
            {isSubmitting ? "Saving..." : "Save"}
          </Button>
        </Stack>
      </Stack>
    </Paper>
  );
};
