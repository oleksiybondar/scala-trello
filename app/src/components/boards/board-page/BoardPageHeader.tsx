import type { ReactElement } from "react";
import { useNavigate } from "react-router-dom";

import SettingsOutlinedIcon from "@mui/icons-material/SettingsOutlined";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { useBoard } from "@hooks/useBoard";

export const BoardPageHeader = (): ReactElement => {
  const navigate = useNavigate();
  const { board, boardPermissionAccess } = useBoard();
  const canOpenSettings =
    boardPermissionAccess.canModify || boardPermissionAccess.canReassign;

  return (
    <Stack spacing={1}>
      <Stack
        alignItems="center"
        direction="row"
        justifyContent="space-between"
        spacing={2}
      >
        <Typography
          sx={{
            color: "primary.main",
            flex: 1,
            minWidth: 0
          }}
          variant="h2"
        >
          {board?.name ?? "Board"}
        </Typography>

        {canOpenSettings && board !== null ? (
          <IconButton
            aria-label="Board settings"
            onClick={() => {
              void navigate("/boards/" + board.boardId + "/settings");
            }}
          >
            <SettingsOutlinedIcon />
          </IconButton>
        ) : null}
      </Stack>

      {board?.description?.trim() ? (
        <Typography color="text.secondary" sx={{ maxWidth: 920 }} variant="body1">
          {board.description.trim()}
        </Typography>
      ) : null}
    </Stack>
  );
};
