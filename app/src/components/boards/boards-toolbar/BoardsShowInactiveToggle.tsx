import type { ChangeEvent, ReactElement } from "react";

import Box from "@mui/material/Box";
import FormControlLabel from "@mui/material/FormControlLabel";
import Switch from "@mui/material/Switch";

interface BoardsShowInactiveToggleProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
}

export const BoardsShowInactiveToggle = ({
  checked,
  onChange
}: BoardsShowInactiveToggleProps): ReactElement => {
  return (
    <Box
      sx={{
        alignItems: "center",
        display: "flex"
      }}
    >
      <FormControlLabel
        control={
          <Switch
            checked={checked}
            onChange={(_event: ChangeEvent<HTMLInputElement>, nextChecked: boolean) => {
              onChange(nextChecked);
            }}
          />
        }
        label="Show inactive"
        sx={{
          m: 0,
          whiteSpace: "nowrap"
        }}
      />
    </Box>
  );
};
