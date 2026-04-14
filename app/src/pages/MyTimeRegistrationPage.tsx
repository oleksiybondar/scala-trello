import type { ReactElement } from "react";

import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { AppPageLayout } from "@components/layout/AppPageLayout";

export const MyTimeRegistrationPage = (): ReactElement => {
  return (
    <AppPageLayout>
      <Stack spacing={1.5}>
        <Typography variant="h2">My time registration</Typography>
        <Typography color="text.secondary" variant="body1">
          This page is a stub. Personal time registration tools are coming next.
        </Typography>
      </Stack>
    </AppPageLayout>
  );
};
