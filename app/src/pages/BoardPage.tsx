import type { ReactElement } from "react";

import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { useParams } from "react-router-dom";

import { AppPageLayout } from "@components/layout/AppPageLayout";

export const BoardPage = (): ReactElement => {
  const { boardId } = useParams();

  return (
    <AppPageLayout>
      <Stack spacing={1}>
        <Typography variant="h2">Board</Typography>
        <Typography color="text.secondary" variant="body1">
          Board page stub for {boardId}.
        </Typography>
      </Stack>
    </AppPageLayout>
  );
};
