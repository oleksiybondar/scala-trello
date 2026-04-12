import type { ReactElement } from "react";

import SettingsOutlinedIcon from "@mui/icons-material/SettingsOutlined";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import type { Board } from "../../domain/board/graphql";

interface BoardCardHeaderProps {
  board: Board;
  onOpenSettings: () => void;
  showSettingsButton?: boolean;
}

export const BoardCardHeader = ({
  board,
  onOpenSettings,
  showSettingsButton = true
}: BoardCardHeaderProps): ReactElement => {
  return (
    <Stack
      alignItems="center"
      direction="row"
      justifyContent="space-between"
      spacing={1}
    >
      <Typography
        sx={{
          color: "primary.main",
          flex: 1,
          minWidth: 0,
          overflow: "hidden",
          textOverflow: "ellipsis",
          whiteSpace: "nowrap"
        }}
        variant="h5"
      >
        {board.name}
      </Typography>
      {showSettingsButton ? (
        <IconButton
          aria-label="Board settings"
          onClick={event => {
            event.stopPropagation();
            onOpenSettings();
          }}
          size="small"
        >
          <SettingsOutlinedIcon fontSize="small" />
        </IconButton>
      ) : null}
    </Stack>
  );
};
