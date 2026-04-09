import type { ReactElement } from "react";

import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

export const NoBoardsFound = (): ReactElement => {
  return (
    <Paper sx={{ p: { md: 5, xs: 3 } }} variant="outlined">
      <Stack alignItems="center" spacing={1.5}>
        <Typography textAlign="center" variant="h3">
          No boards found.
        </Typography>
        <Typography color="text.secondary" textAlign="center" variant="body1">
          Try changing the search term or owner filter.
        </Typography>
      </Stack>
    </Paper>
  );
};
