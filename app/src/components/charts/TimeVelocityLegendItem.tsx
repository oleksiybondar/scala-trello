import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";

interface TimeVelocityLegendItemProps {
  color: string;
  label: string;
  value: string;
}

export const TimeVelocityLegendItem = ({
  color,
  label,
  value
}: TimeVelocityLegendItemProps): ReactElement => {
  return (
    <Box
      sx={{
        alignItems: "center",
        display: "flex",
        gap: 0.75
      }}
    >
      <Box
        sx={{
          backgroundColor: color,
          borderRadius: 999,
          height: 3,
          width: 20
        }}
      />
      <Typography color="text.secondary" variant="caption">
        {label}: {value}
      </Typography>
    </Box>
  );
};
