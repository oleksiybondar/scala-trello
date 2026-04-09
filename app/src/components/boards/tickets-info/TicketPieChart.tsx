import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import { alpha, useTheme } from "@mui/material/styles";

interface TicketPieChartSegment {
  color: string;
  value: number;
}

interface TicketPieChartProps {
  segments: TicketPieChartSegment[];
  total: number;
}

export const TicketPieChart = ({
  segments,
  total
}: TicketPieChartProps): ReactElement => {
  const theme = useTheme();
  let currentPercent = 0;

  const chartBackground = `conic-gradient(${segments
    .map(segment => {
      const startPercent = currentPercent;
      const endPercent =
        total === 0 ? currentPercent : currentPercent + (segment.value / total) * 100;

      currentPercent = endPercent;

      return (
        segment.color +
        " " +
        String(startPercent) +
        "% " +
        String(endPercent) +
        "%"
      );
    })
    .join(", ")})`;

  return (
    <Box
      sx={{
        alignItems: "center",
        background: chartBackground,
        borderRadius: "50%",
        display: "flex",
        height: 132,
        justifyContent: "center",
        width: 132
      }}
    >
      <Box
        sx={{
          alignItems: "center",
          backgroundColor: "background.paper",
          borderRadius: "50%",
          boxShadow: `inset 0 0 0 1px ${alpha(theme.palette.divider, 0.7)}`,
          display: "flex",
          flexDirection: "column",
          height: 82,
          justifyContent: "center",
          width: 82
        }}
      >
        <Typography variant="h6">{total}</Typography>
        <Typography color="text.secondary" variant="caption">
          Total
        </Typography>
      </Box>
    </Box>
  );
};
