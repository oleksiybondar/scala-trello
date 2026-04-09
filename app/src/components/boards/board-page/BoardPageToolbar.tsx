import type { ReactElement } from "react";

import Paper from "@mui/material/Paper";

export const BoardPageToolbar = (): ReactElement => {
  return (
    <Paper
      sx={{
        minHeight: 56
      }}
      variant="outlined"
    />
  );
};
