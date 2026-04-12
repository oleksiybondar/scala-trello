export const TIME_INPUT_STEP_MINUTES = 15;

export const formatMinutesToTimeInput = (value: number | null): string => {
  if (value === null || value <= 0) {
    return "";
  }

  const hours = Math.floor(value / 60);
  const minutes = value % 60;

  return String(hours).padStart(2, "0") + ":" + String(minutes).padStart(2, "0");
};

export const parseTimeInputToMinutes = (value: string): number | null => {
  const normalized = value.trim();

  if (normalized.length === 0) {
    return null;
  }

  if (/^\d+$/.test(normalized)) {
    return Number.parseInt(normalized, 10) * 60;
  }

  const match = /^(\d+):(\d{1,2})$/.exec(normalized);

  if (match === null) {
    return null;
  }

  const hours = Number.parseInt(match[1] ?? "", 10);
  const minutes = Number.parseInt(match[2] ?? "", 10);

  if (Number.isNaN(hours) || Number.isNaN(minutes) || minutes >= 60) {
    return null;
  }

  return hours * 60 + minutes;
};

export const formatMinutesToHoursField = (value: number | null): string => {
  if (value === null || value <= 0) {
    return "";
  }

  return String(Math.floor(value / 60));
};

export const formatMinutesToMinutesField = (value: number | null): string => {
  if (value === null || value <= 0) {
    return "";
  }

  return String(value % 60).padStart(2, "0");
};

export const formatMinutesToTimeTrackingDuration = (value: number): string => {
  const safeMinutes = Math.max(0, value);
  const hours = Math.floor(safeMinutes / 60);
  const minutes = safeMinutes % 60;

  return String(hours) + "h:" + String(minutes).padStart(2, "0") + "m";
};

export const parseTimeTrackingDurationToMinutes = (value: string): number => {
  const normalized = value.trim().toLowerCase();
  const durationPattern = /^(?:(\d+)\s*h)?(?::?(\d+)\s*m?)?$/;
  const match = durationPattern.exec(normalized);

  if (match === null) {
    return 0;
  }

  const hours = Number.parseInt(match[1] ?? "0", 10);
  const minutes = Number.parseInt(match[2] ?? "0", 10);

  return hours * 60 + minutes;
};
