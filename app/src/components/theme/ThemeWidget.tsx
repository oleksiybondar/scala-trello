import type { ReactElement } from "react";

import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { ThemeModeToggle } from "@components/theme/ThemeModeToggle";
import { ThemeSourceSelector } from "@components/theme/ThemeSourceSelector";
import { ThemeTemplateSelector } from "@components/theme/ThemeTemplateSelector";

export const ThemeWidget = (): ReactElement => {
  return (
    <Paper>
      <Stack spacing={3} p={3}>
        <Stack spacing={1}>
          <Typography variant="h6">Theme widget</Typography>
          <Typography color="textSecondary" variant="body2">
            Theme management is split between source selection, user overrides,
            and resolved theme output.
          </Typography>
        </Stack>

        <ThemeSourceSelector />
        <ThemeModeToggle />
        <ThemeTemplateSelector />
      </Stack>
    </Paper>
  );
};
