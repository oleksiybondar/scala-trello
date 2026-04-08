import type { ReactElement } from "react";

import AddRoundedIcon from "@mui/icons-material/AddRounded";
import Button from "@mui/material/Button";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

interface NoBoardsProps {
  onCreateBoard: () => void;
}

/**
 * Empty state shown when the current user has no boards yet.
 */
export const NoBoards = ({ onCreateBoard }: NoBoardsProps): ReactElement => {
  return (
    <Paper sx={{ p: { md: 5, xs: 3 } }} variant="outlined">
      <Stack alignItems="center" spacing={3}>
        <Stack maxWidth={760} spacing={1} textAlign="center" width="100%">
          <Typography variant="h3">You don&apos;t have any boards yet.</Typography>
          <Typography color="text.secondary" variant="body1">
            Someone can invite you to an existing board, or you can create your
            own board and start organizing sprint work there.
          </Typography>
        </Stack>

        <Button
          onClick={onCreateBoard}
          size="large"
          startIcon={<AddRoundedIcon />}
          variant="contained"
        >
          Create board
        </Button>
      </Stack>
    </Paper>
  );
};
