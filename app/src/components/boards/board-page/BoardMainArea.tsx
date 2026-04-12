import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";

import { BoardColumn } from "@components/boards/board-page/BoardColumn";
import { useTickets } from "@hooks/useTickets";

const BOARD_COLUMN_TITLES = [
  "New",
  "In progress",
  "Code review",
  "In Testing",
  "Done"
] as const;

export const BoardMainArea = (): ReactElement => {
  const {
    codeReviewTickets,
    doneTickets,
    inProgressTickets,
    inTestingTickets,
    newTickets
  } = useTickets();
  const columnTickets = {
    "Code review": codeReviewTickets,
    Done: doneTickets,
    "In Testing": inTestingTickets,
    "In progress": inProgressTickets,
    New: newTickets
  } as const;

  return (
    <Paper
      sx={{
        minHeight: "800px",
        p: 2
      }}
      variant="outlined"
    >
      <Box
        sx={{
          display: "grid",
          gap: 1,
          gridTemplateColumns: {
            lg: "repeat(5, minmax(0, 1fr))",
            xs: "1fr"
          },
          minHeight: "100%"
        }}
      >
        {BOARD_COLUMN_TITLES.map(title => (
          <BoardColumn key={title} tickets={columnTickets[title]} title={title} />
        ))}
      </Box>
    </Paper>
  );
};
