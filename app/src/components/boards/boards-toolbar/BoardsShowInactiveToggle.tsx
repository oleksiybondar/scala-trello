import type { ChangeEvent, ReactElement } from "react";

import Box from "@mui/material/Box";
import Switch from "@mui/material/Switch";
import Typography from "@mui/material/Typography";

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
        display: "flex",
      }}
    >

          <Switch
            checked={checked}
            onChange={(_event: ChangeEvent<HTMLInputElement>, nextChecked: boolean) => {
              onChange(nextChecked);
            }}
          />
            <Typography noWrap>
                Show Inactive
            </Typography>

    </Box>
  );
};
