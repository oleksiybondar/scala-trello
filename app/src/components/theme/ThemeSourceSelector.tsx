import type { ReactElement } from "react";

import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";

import { useThemeManager } from "@hooks/useThemeManager";
import type { ThemeSource } from "@theme/index";

export const ThemeSourceSelector = (): ReactElement => {
  const { setSource, source } = useThemeManager();

  return (
    <FormControl fullWidth>
      <InputLabel id="theme-source-label">Theme source</InputLabel>
      <Select
        label="Theme source"
        labelId="theme-source-label"
        onChange={event => {
          setSource(event.target.value as ThemeSource);
        }}
        value={source}
      >
        <MenuItem value="default">Default</MenuItem>
        <MenuItem value="os">OS</MenuItem>
        <MenuItem value="user">User</MenuItem>
      </Select>
    </FormControl>
  );
};
