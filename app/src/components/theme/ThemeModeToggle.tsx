import type { ReactElement } from "react";

import FormControlLabel from "@mui/material/FormControlLabel";
import Stack from "@mui/material/Stack";
import Switch from "@mui/material/Switch";
import Typography from "@mui/material/Typography";

import { useThemeManager } from "@hooks/useThemeManager";

export const ThemeModeToggle = (): ReactElement => {
  const { resolvedMode, setMode, source } = useThemeManager();
  const isDisabled = source !== "user";

  return (
    <Stack spacing={1}>
      <Typography variant="subtitle2">Theme mode</Typography>
      <FormControlLabel
        control={
          <Switch
            checked={resolvedMode === "dark"}
            disabled={isDisabled}
            onChange={event => {
              setMode(event.target.checked ? "dark" : "light");
            }}
          />
        }
        label={resolvedMode === "dark" ? "Dark" : "Light"}
      />
      <Typography color="textSecondary" variant="caption">
        Switch on means dark mode. Available only for user-controlled theme.
      </Typography>
    </Stack>
  );
};
