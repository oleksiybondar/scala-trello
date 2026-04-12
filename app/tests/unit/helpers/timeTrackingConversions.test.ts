import {
  formatMinutesToTimeInput,
  formatMinutesToTimeTrackingDuration,
  parseTimeInputToMinutes,
  parseTimeTrackingDurationToMinutes,
  TIME_INPUT_STEP_MINUTES
} from "@helpers/timeTrackingConversions";

describe("timeTrackingConversions", () => {
  test("formats minutes for time input", () => {
    expect(formatMinutesToTimeInput(null)).toBe("");
    expect(formatMinutesToTimeInput(15)).toBe("00:15");
    expect(formatMinutesToTimeInput(150)).toBe("02:30");
  });

  test("parses time input into minutes", () => {
    expect(parseTimeInputToMinutes("")).toBeNull();
    expect(parseTimeInputToMinutes("2")).toBe(120);
    expect(parseTimeInputToMinutes("02:30")).toBe(150);
    expect(parseTimeInputToMinutes("02:75")).toBeNull();
  });

  test("formats and parses time tracking duration values", () => {
    expect(formatMinutesToTimeTrackingDuration(0)).toBe("0h:00m");
    expect(formatMinutesToTimeTrackingDuration(135)).toBe("2h:15m");
    expect(parseTimeTrackingDurationToMinutes("2h:15m")).toBe(135);
    expect(parseTimeTrackingDurationToMinutes("3h")).toBe(180);
  });

  test("uses 15 minutes as the standard input step", () => {
    expect(TIME_INPUT_STEP_MINUTES).toBe(15);
  });
});
