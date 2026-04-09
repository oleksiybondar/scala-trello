import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

interface TicketPieChartLegendItemProps {
  color: string;
  count: number;
  title: string;
}

export const TicketPieChartLegendItem = ({
  color,
  count,
  title
}: TicketPieChartLegendItemProps): ReactElement => {
  return (
    <Stack
      alignItems="center"
      direction="row"
      spacing={1}
      sx={{ minWidth: 0 }}
    >
      <Box
        sx={{
          backgroundColor: color,
          borderRadius: "50%",
          flex: "0 0 auto",
          height: 10,
          width: 10
        }}
      />
      <Typography variant="body2">
        {title}: {count}
      </Typography>
    </Stack>
  );
};
