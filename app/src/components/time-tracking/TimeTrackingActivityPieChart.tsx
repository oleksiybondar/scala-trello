import type { ReactElement } from "react";

import type { StackProps } from "@mui/material/Stack";

import { DonutChartWithLegend } from "@components/charts/DonutChartWithLegend";
import { formatMinutesToTimeTrackingDuration } from "@helpers/timeTrackingConversions";
import type { TimeTrackingActivitySlice } from "@helpers/timeTrackingActivities";

interface TimeTrackingActivityPieChartProps {
  activitySlices: TimeTrackingActivitySlice[];
  centerLabel?: string | undefined;
  direction?: StackProps["direction"];
  emptyLegendText?: string | undefined;
  legendLabelResolver?: ((slice: TimeTrackingActivitySlice) => string) | undefined;
  totalActivityMinutes: number;
}

export const TimeTrackingActivityPieChart = ({
  activitySlices,
  centerLabel = "Logged",
  direction,
  emptyLegendText,
  legendLabelResolver,
  totalActivityMinutes
}: TimeTrackingActivityPieChartProps): ReactElement => {
  return (
    <DonutChartWithLegend
      centerLabel={centerLabel}
      centerValue={formatMinutesToTimeTrackingDuration(totalActivityMinutes)}
      direction={direction}
      emptyLegendText={emptyLegendText}
      items={activitySlices.map(slice => ({
        color: slice.color,
        key: slice.key,
        label:
          legendLabelResolver?.(slice) ??
          `${slice.name}: ${formatMinutesToTimeTrackingDuration(slice.minutes)}`,
        value: slice.minutes
      }))}
    />
  );
};
