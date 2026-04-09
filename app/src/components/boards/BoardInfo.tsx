import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { Person } from "@components/avatar/Person";
import type { Board } from "@models/board";

interface BoardInfoProps {
  board: Board;
}

interface InfoRowProps {
  children: ReactElement;
  label: string;
}

const InfoRow = ({ children, label }: InfoRowProps): ReactElement => {
  return (
    <Stack
      alignItems="center"
      direction="row"
      spacing={1.5}
      sx={{
        minWidth: 0
      }}
    >
      <Typography
        color="text.secondary"
        sx={{
          flex: "0 0 84px"
        }}
        variant="body2"
      >
        {label}
      </Typography>
      <Box
        sx={{
          flex: 1,
          minWidth: 0
        }}
      >
        {children}
      </Box>
    </Stack>
  );
};

export const BoardInfo = ({ board }: BoardInfoProps): ReactElement => {
  return (
    <Paper
      sx={{
        borderRadius: 0.5,
        flex: "1 1 320px",
        p: 0.5
      }}
      variant="outlined"
    >
      <Stack spacing={1.25}>
        <InfoRow label="Owned by">
          <Person fallbackLabel="Unknown owner" person={board.owner} />
        </InfoRow>

        <InfoRow label="Created by">
          <Person fallbackLabel="Unknown creator" person={board.createdBy} />
        </InfoRow>

        <InfoRow label="Members">
          <Typography variant="body2">{board.membersCount}</Typography>
        </InfoRow>

        <InfoRow label="Status">
          <Typography color={ board.active ? "success.light" : "grey.300"}>
              {board.active ? "Active" : "Closed"}
          </Typography>
        </InfoRow>
      </Stack>
    </Paper>
  );
};
