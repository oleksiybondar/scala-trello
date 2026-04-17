import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import { alpha, useTheme } from "@mui/material/styles";

import { TimeVelocityLegendItem } from "@components/charts/TimeVelocityLegendItem";
import { formatMinutesToTimeTrackingDuration } from "@helpers/timeTrackingConversions";

export interface TimeVelocityData {
  actualSeriesMinutes: number[];
  estimatedMinutes: number;
}

interface TimeVelocityChartProps {
  chartHeight?: number | undefined;
  data: TimeVelocityData;
}

interface Point {
  x: number;
  y: number;
}

const createPoints = (
  values: number[],
  maxValue: number,
  height: number,
  width: number
): Point[] => {
  const safeValues = values.length === 0 ? [0] : values;
  const stepX = width / Math.max(safeValues.length - 1, 1);

  return safeValues
    .map((value, index) => {
      const clampedValue = Math.max(0, value);

      return {
        x: index * stepX,
        y: height - (clampedValue / maxValue) * height
      };
    });
};

const createPath = (points: Point[]): string | null => {
  if (points.length === 0) {
    return null;
  }

  return points
    .map((point, index) => {
      return `${index === 0 ? "M" : "L"} ${String(point.x)} ${String(point.y)}`;
    })
    .join(" ");
};

const addPoint = (series: Point[], point: Point): void => {
  const previous = series[series.length - 1];

  if (previous?.x === point.x && previous.y === point.y) {
    return;
  }

  series.push(point);
};

const splitActualSeriesByEstimate = (
  points: Point[],
  values: number[],
  estimatedMinutes: number
): {
  normal: Point[];
  overdue: Point[];
} => {
  if (points.length === 0 || values.length === 0) {
    return {
      normal: [],
      overdue: []
    };
  }

  if (points.length === 1) {
    const firstPoint = points[0];
    const firstValue = values[0];

    if (firstPoint === undefined || firstValue === undefined) {
      return {
        normal: [],
        overdue: []
      };
    }

    return firstValue > estimatedMinutes
      ? { normal: [], overdue: [firstPoint] }
      : { normal: [firstPoint], overdue: [] };
  }

  const normal: Point[] = [];
  const overdue: Point[] = [];

  for (let index = 0; index < points.length - 1; index += 1) {
    const startPoint = points[index];
    const endPoint = points[index + 1];
    const startValue = values[index];
    const endValue = values[index + 1];

    if (
      startPoint === undefined ||
      endPoint === undefined ||
      startValue === undefined ||
      endValue === undefined
    ) {
      continue;
    }
    const isStartOverdue = startValue > estimatedMinutes;
    const isEndOverdue = endValue > estimatedMinutes;

    if (isStartOverdue === isEndOverdue) {
      if (isStartOverdue) {
        addPoint(overdue, startPoint);
        addPoint(overdue, endPoint);
      } else {
        addPoint(normal, startPoint);
        addPoint(normal, endPoint);
      }

      continue;
    }

    const progress = (estimatedMinutes - startValue) / (endValue - startValue);
    const crossingPoint: Point = {
      x: startPoint.x + (endPoint.x - startPoint.x) * progress,
      y: startPoint.y + (endPoint.y - startPoint.y) * progress
    };

    if (isStartOverdue) {
      addPoint(overdue, startPoint);
      addPoint(overdue, crossingPoint);
      addPoint(normal, crossingPoint);
      addPoint(normal, endPoint);
    } else {
      addPoint(normal, startPoint);
      addPoint(normal, crossingPoint);
      addPoint(overdue, crossingPoint);
      addPoint(overdue, endPoint);
    }
  }

  return {
    normal,
    overdue
  };
};

export const TimeVelocityChart = ({
  chartHeight = 72,
  data
}: TimeVelocityChartProps): ReactElement => {
  const theme = useTheme();
  const width = 220;
  const height = chartHeight;
  const drawingHeight = height - 8;
  const safeEstimatedMinutes = Math.max(0, data.estimatedMinutes);
  const normalizedActualSeries = data.actualSeriesMinutes.map(value => Math.max(0, value));
  const actualTotal =
    normalizedActualSeries[normalizedActualSeries.length - 1] ?? 0;
  const plottingActualSeries =
    normalizedActualSeries.length === 0 ? [0, 0] : [0, ...normalizedActualSeries];
  const overdueMinutes = Math.max(0, actualTotal - safeEstimatedMinutes);
  const estimatedSeries = plottingActualSeries.map((_, index) => {
    return (safeEstimatedMinutes * index) / (plottingActualSeries.length - 1);
  });
  const maxValue = Math.max(
    safeEstimatedMinutes,
    ...plottingActualSeries,
    1
  );
  const estimatedPoints = createPoints(estimatedSeries, maxValue, drawingHeight, width);
  const actualPoints = createPoints(plottingActualSeries, maxValue, drawingHeight, width);
  const splitActual = splitActualSeriesByEstimate(
    actualPoints,
    plottingActualSeries,
    safeEstimatedMinutes
  );
  const estimatedPath = createPath(estimatedPoints);
  const actualPath = createPath(splitActual.normal);
  const overduePath = createPath(splitActual.overdue);

  return (
    <Box sx={{ display: "flex", flexDirection: "column", gap: 0.75, width: "100%" }}>
      <Box sx={{ minWidth: 0, width: "100%" }}>
        <svg
          aria-label="Time velocity chart"
          height={height}
          viewBox={"0 0 " + String(width) + " " + String(height)}
          width="100%"
        >
          <line
            stroke={alpha(theme.palette.divider, 0.9)}
            strokeWidth="1"
            x1="0"
            x2={width}
            y1={height}
            y2={height}
          />
          {estimatedPath !== null ? (
            <path
              d={estimatedPath}
              fill="none"
              stroke={theme.palette.primary.main}
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="4"
              transform="translate(0 4)"
            />
          ) : null}
          {actualPath !== null ? (
            <path
              d={actualPath}
              fill="none"
              stroke={theme.palette.success.main}
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="4"
              transform="translate(0 4)"
            />
          ) : null}
          {overduePath !== null ? (
            <path
              d={overduePath}
              fill="none"
              stroke={theme.palette.error.main}
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="4"
              transform="translate(0 4)"
            />
          ) : null}
        </svg>
      </Box>

      <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1.5, justifyContent: "center" }}>
        <TimeVelocityLegendItem
          color={theme.palette.primary.main}
          label="Es"
          value={formatMinutesToTimeTrackingDuration(safeEstimatedMinutes)}
        />
        <TimeVelocityLegendItem
          color={theme.palette.success.main}
          label="Act"
          value={formatMinutesToTimeTrackingDuration(actualTotal)}
        />
        {overdueMinutes > 0 ? (
          <TimeVelocityLegendItem
            color={theme.palette.error.main}
            label="Ovd"
            value={formatMinutesToTimeTrackingDuration(overdueMinutes)}
          />
        ) : null}
      </Box>
    </Box>
  );
};
