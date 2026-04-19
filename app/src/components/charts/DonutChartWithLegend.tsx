import type { ReactElement } from "react";

import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import type { StackProps } from "@mui/material/Stack";

import { DonutChart } from "@components/charts/DonutChart";
import { DonutChartLegendItem } from "@components/charts/DonutChartLegendItem";

export interface DonutChartWithLegendItem {
  color: string;
  key: string;
  label: string;
  value: number;
}

interface DonutChartWithLegendProps {
  centerLabel?: string | undefined;
  centerValue: string | number;
  direction?: StackProps["direction"];
  emptyLegendText?: string | undefined;
  items: DonutChartWithLegendItem[];
  spacing?: number | undefined;
}

export const DonutChartWithLegend = ({
  centerLabel = "Total",
  centerValue,
  direction = { md: "row", xs: "column" },
  emptyLegendText,
  items,
  spacing = 2
}: DonutChartWithLegendProps): ReactElement => {
  return (
    <Stack alignItems="center" direction={direction} justifyContent="center" spacing={spacing}>
      <Stack
        alignItems="center"
        justifyContent="center"
        spacing={1}
        sx={{ flex: "0 0 160px", minWidth: 140 }}
      >
        <DonutChart
          centerLabel={centerLabel}
          centerValue={centerValue}
          segments={items.map(item => ({
            color: item.color,
            value: item.value
          }))}
        />
      </Stack>

      <Stack spacing={1} sx={{ flex: 1, minWidth: 0 }}>
        {items.length === 0 && emptyLegendText !== undefined ? (
          <Typography color="text.secondary" variant="body2">
            {emptyLegendText}
          </Typography>
        ) : (
          items.map(item => (
            <DonutChartLegendItem color={item.color} key={item.key} value={item.label} />
          ))
        )}
      </Stack>
    </Stack>
  );
};
