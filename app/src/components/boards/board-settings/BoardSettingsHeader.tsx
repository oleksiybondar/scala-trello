import type { ReactElement } from "react";

import Link from "@mui/material/Link";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { Link as RouterLink, useParams } from "react-router-dom";

import { useBoard } from "@hooks/useBoard";

export const BoardSettingsHeader = (): ReactElement => {
  const { boardId = "" } = useParams();
  const { board } = useBoard();
  const boardName = board?.name ?? "Board";

  return (
    <Stack spacing={0.5}>
      <Link
        color="primary"
        component={RouterLink}
        sx={{
          textDecoration: "none"
        }}
        to={"/boards/" + boardId}
        underline="none"
      >
        <Typography
          component="span"
          sx={{
            fontSize: theme => theme.typography.h2.fontSize,
            fontWeight: theme => theme.typography.h2.fontWeight,
            lineHeight: theme => theme.typography.h2.lineHeight
          }}
          variant="h2"
        >
          {boardName}
        </Typography>
        <Typography
          component="span"
          sx={{
            color: "text.secondary",
            fontSize: theme => theme.typography.body2.fontSize,
            fontWeight: 500,
            ml: 1.25,
            verticalAlign: "middle"
          }}
          variant="body2"
        >
          ({boardId})
        </Typography>
      </Link>
      <Typography color="text.secondary" variant="body1">
        Configure board settings and membership access.
      </Typography>
    </Stack>
  );
};
