import type { ChangeEvent, ReactElement, SyntheticEvent } from "react";
import { useEffect, useState } from "react";

import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { TimeTrackingActivitySelect } from "@components/time-tracking/TimeTrackingActivitySelect";
import { TimeInput } from "@components/time-tracking/TimeInput";
import { useActivities } from "@hooks/useActivities";
import { useTimeTracking } from "@hooks/useTimeTracking";

interface TimeTrackingModalProps {
  isOpen: boolean;
  onClose: () => void;
  ticketId: string | null;
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

export const TimeTrackingModal = ({
  isOpen,
  onClose,
  ticketId
}: TimeTrackingModalProps): ReactElement => {
  const {
    activities,
    activitiesError,
    hasLoadedActivities,
    isLoadingActivities,
    loadActivities
  } = useActivities();
  const { isRegisteringTime, registerTime } = useTimeTracking();
  const [selectedActivityId, setSelectedActivityId] = useState("");
  const [durationMinutes, setDurationMinutes] = useState<number | null>(null);
  const [loggedAt, setLoggedAt] = useState(toDateTimeLocalValue(new Date()));
  const [description, setDescription] = useState("");
  const [submitError, setSubmitError] = useState<string | null>(null);

  useEffect(() => {
    if (!isOpen || hasLoadedActivities || isLoadingActivities) {
      return;
    }

    void loadActivities();
  }, [hasLoadedActivities, isLoadingActivities, isOpen, loadActivities]);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    setSubmitError(null);
    setDurationMinutes(null);
    setDescription("");
    setLoggedAt(toDateTimeLocalValue(new Date()));
    setSelectedActivityId(previousActivityId => {
      if (activities.some(activity => activity.activityId === previousActivityId)) {
        return previousActivityId;
      }

      return activities[0]?.activityId ?? "";
    });
  }, [activities, isOpen]);

  const isActivityValid = selectedActivityId.length > 0;
  const isDurationValid = durationMinutes !== null && durationMinutes > 0;
  const loggedAtIso = parseDateTimeLocalToIso(loggedAt);
  const isLoggedAtValid = loggedAtIso !== null;
  const canSubmit =
    ticketId !== null &&
    isActivityValid &&
    isDurationValid &&
    isLoggedAtValid &&
    !isRegisteringTime &&
    !isLoadingActivities;

  const handleSubmit = async (event: SyntheticEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();

    if (isRegisteringTime || isLoadingActivities || !isActivityValid || !isDurationValid) {
      return;
    }

    if (loggedAtIso === null || ticketId === null) {
      return;
    }

    const parsedActivityId = Number.parseInt(selectedActivityId, 10);

    if (Number.isNaN(parsedActivityId)) {
      setSubmitError("Selected activity is invalid.");
      return;
    }

    setSubmitError(null);

    try {
      await registerTime({
        activityId: parsedActivityId,
        description,
        durationMinutes,
        loggedAt: loggedAtIso,
        ticketId
      });
      onClose();
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Failed to register time.");
    }
  };

  return (
    <Dialog fullWidth maxWidth="sm" onClose={onClose} open={isOpen}>
      <DialogTitle>Log time</DialogTitle>
      <Stack component="form" onSubmit={handleSubmit} spacing={0}>
        <DialogContent dividers>
          <Stack spacing={3}>
            {activitiesError !== null ? <Alert severity="error">{activitiesError.message}</Alert> : null}
            {submitError !== null ? <Alert severity="error">{submitError}</Alert> : null}

            {ticketId === null ? (
              <Alert severity="warning">No ticket selected for time tracking.</Alert>
            ) : (
              <Typography color="text.secondary" variant="body2">
                Ticket id: {ticketId}
              </Typography>
            )}

            <TimeTrackingActivitySelect
              activities={activities}
              disabled={isLoadingActivities || isRegisteringTime}
              onChange={setSelectedActivityId}
              value={selectedActivityId}
            />

            <TimeInput
              disabled={isRegisteringTime}
              label="Duration (HH:MM)"
              onChange={setDurationMinutes}
              value={durationMinutes}
            />

            <TextField
              disabled={isRegisteringTime}
              error={!isLoggedAtValid}
              fullWidth
              helperText={isLoggedAtValid ? "Choose when the work happened." : "Invalid date/time value."}
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
              disabled={isRegisteringTime}
              fullWidth
              label="Description"
              minRows={3}
              multiline
              onChange={(event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
                setDescription(event.target.value);
              }}
              value={description}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} variant="outlined">
            Cancel
          </Button>
          <Button disabled={!canSubmit} type="submit" variant="contained">
            {isRegisteringTime ? "Saving..." : "Register time"}
          </Button>
        </DialogActions>
      </Stack>
    </Dialog>
  );
};
