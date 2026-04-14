import { useRef, useState } from "react";
import type { DragEvent, ReactElement } from "react";

import ArrowDownwardRoundedIcon from "@mui/icons-material/ArrowDownwardRounded";
import ArrowUpwardRoundedIcon from "@mui/icons-material/ArrowUpwardRounded";
import Box from "@mui/material/Box";
import IconButton from "@mui/material/IconButton";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import { alpha } from "@mui/material/styles";

import { BoardTicketCard } from "@components/boards/board-page/BoardTicketCard";
import type { Ticket } from "../../../domain/ticket/graphql";
import type { TicketPrioritySorting } from "../../../domain/ticket/useTicketsFilteringService";

interface BoardColumnProps {
  background?: string;
  onPriorityDirectionToggle?: () => void;
  onDrop?: (ticketId: string) => void;
  priorityDirection?: TicketPrioritySorting;
  tickets: Ticket[];
  title: string;
}

const DRAGGED_TICKET_ID_DATA_KEY = "application/x-board-ticket-id";

export const BoardColumn = ({
  background,
  onPriorityDirectionToggle,
  onDrop,
  priorityDirection = "high_to_low",
  tickets,
  title
}: BoardColumnProps): ReactElement => {
  const [isDropTarget, setIsDropTarget] = useState(false);
  const dragPreviewRef = useRef<HTMLDivElement | null>(null);

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
    const preview = event.currentTarget.cloneNode(true);

    if (preview instanceof HTMLDivElement) {
      const rect = event.currentTarget.getBoundingClientRect();

      preview.style.boxSizing = "border-box";
      preview.style.left = "-9999px";
      preview.style.margin = "0";
      preview.style.opacity = "1";
      preview.style.pointerEvents = "none";
      preview.style.position = "fixed";
      preview.style.top = "0";
      preview.style.width = `${String(rect.width)}px`;
      preview.style.zIndex = "9999";

      document.body.append(preview);
      dragPreviewRef.current = preview;
      event.dataTransfer.setDragImage(preview, 24, 24);
    }

    event.dataTransfer.effectAllowed = "move";
    event.dataTransfer.setData(DRAGGED_TICKET_ID_DATA_KEY, ticketId);
    event.dataTransfer.setData("text/plain", ticketId);
  };

  const handleTicketDragEnd = (): void => {
    dragPreviewRef.current?.remove();
    dragPreviewRef.current = null;
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
              : alpha(background, 0.4),
          borderBottom: theme => "1px solid " + theme.palette.divider,
          position: "sticky",
          px: 2,
          py: 1.5,
          top: 0,
          zIndex: 1
        }}
      >
        <Stack alignItems="center" direction="row" justifyContent="space-between" spacing={1}>
          <Typography variant="h6">{title}</Typography>
          <Tooltip
            title={
              priorityDirection === "high_to_low"
                ? "Priority: highest first"
                : "Priority: lowest first"
            }
          >
            <IconButton
              aria-label={`Toggle priority direction for ${title}`}
              onClick={() => {
                onPriorityDirectionToggle?.();
              }}
              size="small"
            >
              {priorityDirection === "high_to_low" ? (
                <ArrowUpwardRoundedIcon fontSize="small" />
              ) : (
                <ArrowDownwardRoundedIcon fontSize="small" />
              )}
            </IconButton>
          </Tooltip>
        </Stack>
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
            <BoardTicketCard
              key={ticket.ticketId}
              onDragEnd={handleTicketDragEnd}
              onDragStart={handleTicketDragStart}
              ticket={ticket}
            />
          ))
        )}
      </Stack>
    </Paper>
  );
};
