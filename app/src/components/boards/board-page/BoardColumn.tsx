import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

interface BoardColumnProps {
  title: string;
}

export const BoardColumn = ({ title }: BoardColumnProps): ReactElement => {
  return (
    <Paper
      sx={{
        bgcolor: "background.default",
        display: "flex",
        flexDirection: "column",
        minHeight: {
          lg: "750px",
          xs: "320px"
        },
        overflow: "hidden"
      }}
      variant="outlined"
    >
      <Box
        sx={{
          backdropFilter: "blur(10px)",
          bgcolor: "background.paper",
          borderBottom: theme => "1px solid " + theme.palette.divider,
          position: "sticky",
          px: 2,
          py: 1.5,
          top: 0,
          zIndex: 1
        }}
      >
        <Typography variant="h6">{title}</Typography>
      </Box>

      <Stack
        justifyContent="center"
        spacing={1}
        sx={{
          color: "text.secondary",
          flex: 1,
          minHeight: 280,
          px: 2,
          py: 3
        }}
      >
        <Typography variant="body2">No tickets yet.</Typography>
      </Stack>
    </Paper>
  );
};
