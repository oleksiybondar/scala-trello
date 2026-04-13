import { useState } from "react";
import type { DragEvent, ReactElement } from "react";

import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Divider from "@mui/material/Divider";
import Typography from "@mui/material/Typography";
import { alpha } from "@mui/material/styles";

import type { Ticket } from "../../../domain/ticket/graphql";

interface BoardColumnProps {
  background?: string;
  onDrop?: (ticketId: string) => void;
  tickets: Ticket[];
  title: string;
}

const DRAGGED_TICKET_ID_DATA_KEY = "application/x-board-ticket-id";

export const BoardColumn = ({
  background,
  onDrop,
  tickets,
  title
}: BoardColumnProps): ReactElement => {
  const [isDropTarget, setIsDropTarget] = useState(false);

  const handleDragOver = (event: DragEvent<HTMLDivElement>): void => {
    event.preventDefault();
    event.dataTransfer.dropEffect = "move";

    if (!isDropTarget) {
      setIsDropTarget(true);
    }
  };

  const handleDragLeave = (event: DragEvent<HTMLDivElement>): void => {
    if (event.currentTarget.contains(event.relatedTarget as Node | null)) {
      return;
    }

    setIsDropTarget(false);
  };

  const handleDrop = (event: DragEvent<HTMLDivElement>): void => {
    event.preventDefault();

    const ticketId =
      event.dataTransfer.getData(DRAGGED_TICKET_ID_DATA_KEY) ||
      event.dataTransfer.getData("text/plain");

    setIsDropTarget(false);

    if (ticketId.length === 0) {
      return;
    }

    onDrop?.(ticketId);
  };

  const handleTicketDragStart = (
    event: DragEvent<HTMLDivElement>,
    ticketId: string
  ): void => {
    event.dataTransfer.effectAllowed = "move";
    event.dataTransfer.setData(DRAGGED_TICKET_ID_DATA_KEY, ticketId);
    event.dataTransfer.setData("text/plain", ticketId);
  };

  const handleTicketDragEnd = (): void => {
    setIsDropTarget(false);
  };

  return (
    <Paper
      sx={{
        bgcolor: "background.default",
        display: "flex",
        flexDirection: "column",
        minHeight: {
          lg: "750px",
          xs: "320px"
        },
        overflow: "hidden"
      }}
      variant="outlined"
    >
      <Box
        sx={{
          backdropFilter: "blur(10px)",
          bgcolor: theme =>
            background === undefined
              ? theme.palette.background.paper
              : alpha(background, 0.14),
          borderBottom: theme => "1px solid " + theme.palette.divider,
          position: "sticky",
          px: 2,
          py: 1.5,
          top: 0,
          zIndex: 1
        }}
      >
        <Typography variant="h6">{title}</Typography>
      </Box>

      <Stack
        spacing={1}
        sx={{
          color: "text.secondary",
          flex: 1,
          minHeight: 280,
          outline: theme =>
            isDropTarget ? "2px dashed " + theme.palette.primary.main : "2px dashed transparent",
          outlineOffset: "-2px",
          px: 2,
          py: 3
        }}
        onDragLeave={handleDragLeave}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
      >
        {tickets.length === 0 ? (
          <Typography variant="body2">No tickets yet.</Typography>
        ) : (
          tickets.map(ticket => (
            <Paper
              draggable
              key={ticket.ticketId}
              onDragEnd={handleTicketDragEnd}
              onDragStart={event => {
                handleTicketDragStart(event, ticket.ticketId);
              }}
              sx={{
                cursor: "grab",
                p: 1.5
              }}
              variant="outlined"
            >
              <Stack spacing={1}>
                <Typography color="text.primary" fontWeight={700} variant="body2">
                  {ticket.name}
                </Typography>
                {ticket.description !== null && ticket.description.trim().length > 0 ? (
                  <Typography variant="caption">{ticket.description}</Typography>
                ) : null}
                <Divider />
                <Stack direction="row" justifyContent="space-between" spacing={1}>
                  <Typography variant="caption">
                    {ticket.assignedTo === null
                      ? "Unassigned"
                      : ticket.assignedTo.firstName + " " + ticket.assignedTo.lastName}
                  </Typography>
                  <Typography variant="caption">
                    {String(ticket.trackedMinutes)}m
                  </Typography>
                </Stack>
              </Stack>
            </Paper>
          ))
        )}
      </Stack>
    </Paper>
  );
};
