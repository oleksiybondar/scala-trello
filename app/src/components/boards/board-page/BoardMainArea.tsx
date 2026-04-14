import type { ReactElement } from "react";

import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import { useTheme } from "@mui/material/styles";

import { BoardColumn } from "@components/boards/board-page/BoardColumn";
import { useTickets } from "@hooks/useTickets";
import {
  boardTicketStates,
  resolveBoardTicketStateColor
} from "@helpers/boardTicketState";
import type { TicketsServiceStatus } from "../../../domain/ticket/useTicketsService";
import type { TicketStatusKey } from "../../../domain/ticket/useTicketsService";

export const BoardMainArea = (): ReactElement => {
  const theme = useTheme();
  const {
    columnPriorityDirections,
    codeReviewTickets,
    doneTickets,
    inProgressTickets,
    inTestingTickets,
    newTickets,
    setColumnPriorityDirection,
    transitionTicketState
  } = useTickets();
  const columnTickets = {
    code_review: codeReviewTickets,
    done: doneTickets,
    in_progress: inProgressTickets,
    in_testing: inTestingTickets,
    new: newTickets
  } as const;
  const handleColumnDrop = (
    ticketId: string,
    status: TicketsServiceStatus
  ): void => {
    void transitionTicketState(ticketId, status);
  };
  const handleToggleColumnDirection = (columnKey: TicketStatusKey): void => {
    setColumnPriorityDirection(
      columnKey,
      columnPriorityDirections[columnKey] === "high_to_low" ? "low_to_high" : "high_to_low"
    );
  };

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
        {boardTicketStates.map(column => (
          <BoardColumn
            background={resolveBoardTicketStateColor(theme, column.paletteColor)}
            key={column.title}
            onDrop={ticketId => {
              handleColumnDrop(ticketId, column.status);
            }}
            onPriorityDirectionToggle={() => {
              handleToggleColumnDirection(column.key);
            }}
            priorityDirection={columnPriorityDirections[column.key]}
            tickets={columnTickets[column.key]}
            title={column.title}
          />
        ))}
      </Box>
    </Paper>
  );
};
