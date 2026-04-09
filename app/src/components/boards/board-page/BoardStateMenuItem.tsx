import type { MouseEvent, ReactElement } from "react";
import { useState } from "react";

import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import Menu from "@mui/material/Menu";
import MenuItem from "@mui/material/MenuItem";
import OutlinedInput from "@mui/material/OutlinedInput";

import { useBoard } from "@hooks/useBoard";

export const BoardStateMenuItem = (): ReactElement => {
  const { board } = useBoard();
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
  const stateLabel = board?.active === false ? "Closed" : "Active";

  const handleOpen = (event: MouseEvent<HTMLElement>): void => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = (): void => {
    setAnchorEl(null);
  };

  return (
    <>
      <OutlinedInput
        endAdornment={<ArrowDropDownIcon color="action" />}
        onClick={handleOpen}
        readOnly
        size="small"
        sx={{
          "& .MuiOutlinedInput-input": {
            cursor: "pointer",
            fontWeight: 600,
            py: 1
          },
          minWidth: 144
        }}
        value={stateLabel}
      />

      <Menu anchorEl={anchorEl} onClose={handleClose} open={anchorEl !== null}>
        <MenuItem disabled selected={board?.active !== false}>
          Active
        </MenuItem>
        <MenuItem disabled selected={board?.active === false}>
          Closed
        </MenuItem>
      </Menu>
    </>
  );
};
