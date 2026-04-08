import type { ReactElement } from "react";

import Container from "@mui/material/Container";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

/**
 * Minimal application footer for the shared page shell.
 */
export const AppFooter = (): ReactElement => {
  return (
    <Container component="footer" maxWidth="lg">
      <Stack
        alignItems={{ sm: "center", xs: "flex-start" }}
        direction={{ sm: "row", xs: "column" }}
        justifyContent="space-between"
        py={3}
        spacing={1}
      >
        <Typography color="text.secondary" variant="body2">
          Boards
        </Typography>
        <Typography color="text.secondary" variant="body2">
          Lightweight sprint board training project
        </Typography>
      </Stack>
    </Container>
  );
};
