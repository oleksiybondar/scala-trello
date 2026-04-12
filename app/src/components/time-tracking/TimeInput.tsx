import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import AddOutlinedIcon from "@mui/icons-material/AddOutlined";
import RemoveOutlinedIcon from "@mui/icons-material/RemoveOutlined";
import IconButton from "@mui/material/IconButton";
import InputAdornment from "@mui/material/InputAdornment";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";

import {
  formatMinutesToTimeInput,
  parseTimeInputToMinutes,
  TIME_INPUT_STEP_MINUTES
} from "@helpers/timeTrackingConversions";

interface TimeInputProps {
  disabled?: boolean | undefined;
  label?: string | undefined;
  onChange: (value: number | null) => void;
  value: number | null;
}

export const TimeInput = ({
  disabled = false,
  label = "Time (HH:MM)",
  onChange,
  value
}: TimeInputProps): ReactElement => {
  const [rawDuration, setRawDuration] = useState(formatMinutesToTimeInput(value));
  const [isManualDurationEdit, setIsManualDurationEdit] = useState(false);

  useEffect(() => {
    if (!isManualDurationEdit) {
      setRawDuration(formatMinutesToTimeInput(value));
    }
  }, [isManualDurationEdit, value]);

  const durationError =
    rawDuration.trim().length > 0 && parseTimeInputToMinutes(rawDuration) === null;

  const updateMinutes = (nextValue: number | null): void => {
    onChange(nextValue === null || nextValue <= 0 ? null : nextValue);
  };

  const handleDurationChange = (
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    const nextRawValue = event.target.value;

    setIsManualDurationEdit(true);
    setRawDuration(nextRawValue);

    const parsedValue = parseTimeInputToMinutes(nextRawValue);

    if (parsedValue !== null || nextRawValue.trim().length === 0) {
      updateMinutes(parsedValue);
    }
  };

  const handleDurationBlur = (): void => {
    setIsManualDurationEdit(false);
    setRawDuration(formatMinutesToTimeInput(value));
  };

  const adjustMinutes = (delta: number): void => {
    const nextValue = Math.max(0, (value ?? 0) + delta);
    const normalizedValue = nextValue === 0 ? null : nextValue;

    setIsManualDurationEdit(false);
    updateMinutes(normalizedValue);
    setRawDuration(formatMinutesToTimeInput(normalizedValue));
  };

  return (
      <TextField
        disabled={disabled}
        error={durationError}
        fullWidth
        helperText={
          durationError
            ? "Use HH:MM or just hours, for example 02:30 or 2."
            : "Enter HH:MM. If you omit ':', the value is treated as hours."
        }
        label={label}
        onBlur={handleDurationBlur}
        onChange={handleDurationChange}
        slotProps={{
          input: {
            endAdornment: (
              <InputAdornment position="end">
                <Stack direction="row" spacing={0.5}>
                  <IconButton
                    aria-label="Decrease estimate by 15 minutes"
                    disabled={disabled}
                    onClick={() => {
                      adjustMinutes(-TIME_INPUT_STEP_MINUTES);
                    }}
                    size="small"
                  >
                    <RemoveOutlinedIcon fontSize="small" />
                  </IconButton>
                  <IconButton
                    aria-label="Increase estimate by 15 minutes"
                    disabled={disabled}
                    onClick={() => {
                      adjustMinutes(TIME_INPUT_STEP_MINUTES);
                    }}
                    size="small"
                  >
                    <AddOutlinedIcon fontSize="small" />
                  </IconButton>
                </Stack>
              </InputAdornment>
            )
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
        value={rawDuration}
      />
  );
};
