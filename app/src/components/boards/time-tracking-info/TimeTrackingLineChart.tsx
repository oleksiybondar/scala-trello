import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import { alpha, useTheme } from "@mui/material/styles";

import { TimeTrackingLegendItem } from "@components/boards/time-tracking-info/TimeTrackingLegendItem";
import type { TimeTrackingStats } from "@components/boards/time-tracking-info/types";

interface TimeTrackingLineChartProps {
  stats: TimeTrackingStats;
}

interface ChartSeries {
  color: string;
  label: string;
  value: string;
  values: number[];
}

const parseDurationToMinutes = (value: string): number => {
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

const createPath = (values: number[], height: number, width: number): string => {
  const maxValue = Math.max(...values, 1);
  const stepX = width / Math.max(values.length - 1, 1);

  return values
    .map((value, index) => {
      const x = index * stepX;
      const y = height - (value / maxValue) * height;

      return (index === 0 ? "M" : "L") + " " + String(x) + " " + String(y);
    })
    .join(" ");
};

export const TimeTrackingLineChart = ({
  stats
}: TimeTrackingLineChartProps): ReactElement => {
  const theme = useTheme();
  const width = 220;
  const height = 72;
  const estimatedMinutes = parseDurationToMinutes(stats.estimatedTime);
  const loggedMinutes = parseDurationToMinutes(stats.loggedTime);

  const series: ChartSeries[] = [
    {
      color: theme.palette.primary.main,
      label: "Es",
      value: stats.estimatedTime,
      values: [estimatedMinutes * 0.35, estimatedMinutes * 0.7, estimatedMinutes]
    },
    {
      color: theme.palette.success.main,
      label: "Act",
      value: stats.loggedTime,
      values: [loggedMinutes * 0.25, loggedMinutes * 0.6, loggedMinutes]
    }
  ];

  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        gap: 0.75,
        width: "100%"
      }}
    >
      <Box sx={{ minWidth: 0, width: "100%" }}>
        <svg
          aria-label="Time tracking line chart"
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
          {series.map(item => (
            <path
              d={createPath(item.values, height - 8, width)}
              fill="none"
              key={item.label}
              stroke={item.color}
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="4"
              transform="translate(0 4)"
            />
          ))}
        </svg>
      </Box>

      <Box
        sx={{
          display: "flex",
          flexWrap: "wrap",
          gap: 1.5,
          justifyContent: "center"
        }}
      >
        {series.map(item => (
          <TimeTrackingLegendItem
            color={item.color}
            key={item.label}
            label={item.label}
            value={item.value}
          />
        ))}
      </Box>
    </Box>
  );
};
