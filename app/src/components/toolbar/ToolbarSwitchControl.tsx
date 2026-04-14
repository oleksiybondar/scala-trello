import type { ChangeEvent, ReactElement } from "react";

import Box from "@mui/material/Box";
import FormControlLabel from "@mui/material/FormControlLabel";
import Switch from "@mui/material/Switch";

interface ToolbarSwitchControlProps {
  checked: boolean;
  disabled?: boolean | undefined;
  label: string;
  onChange: (checked: boolean) => void;
  size?: "medium" | "small" | undefined;
}

export const ToolbarSwitchControl = ({
  checked,
  disabled = false,
  label,
  onChange,
  size = "medium"
}: ToolbarSwitchControlProps): ReactElement => {
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
            disabled={disabled}
            onChange={(_event: ChangeEvent<HTMLInputElement>, nextChecked: boolean) => {
              onChange(nextChecked);
            }}
            size={size}
          />
        }
        label={label}
        sx={{
          m: 0,
          whiteSpace: "nowrap"
        }}
      />
    </Box>
  );
};
