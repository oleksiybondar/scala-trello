import type { ReactElement } from "react";

import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import { useNavigate } from "react-router-dom";

import { BoardCardHeader } from "@components/boards/BoardCardHeader";
import { BoardInfo } from "@components/boards/BoardInfo";
import { TicketsInfo } from "@components/boards/TicketsInfo";
import { TimeTrackingInfo } from "@components/boards/TimeTrackingInfo";
import { canManageBoardSettings } from "@features/board/boardPermissions";
import { formatMinutesToTimeTrackingDuration } from "@helpers/timeTrackingConversions";
import type { Board } from "@models/board";
import type { TicketStateCounts } from "@components/boards/tickets-info/types";
import type { TimeTrackingStats } from "@components/boards/time-tracking-info/types";

interface BoardCardProps {
  board: Board;
}

const createEmptyTicketCounts = (): TicketStateCounts => {
  return {
    code_review: 0,
    done: 0,
    in_progress: 0,
    in_testing: 0,
    new: 0
  };
};

const mapTicketStatusToStateKey = (
  status: string | null
): keyof TicketStateCounts | null => {
  switch (status?.trim().toLowerCase()) {
    case "new":
      return "new";
    case "in progress":
    case "in_progress":
      return "in_progress";
    case "code review":
    case "code_review":
      return "code_review";
    case "in testing":
    case "in_testing":
      return "in_testing";
    case "done":
      return "done";
    default:
      return null;
  }
};

const getTicketCounts = (board: Board): TicketStateCounts => {
  return board.tickets.reduce((counts, ticket) => {
    const stateKey = mapTicketStatusToStateKey(ticket.status);

    if (stateKey === null) {
      return counts;
    }

    return {
      ...counts,
      [stateKey]: counts[stateKey] + 1
    };
  }, createEmptyTicketCounts());
};

const getTimeTrackingStats = (board: Board): TimeTrackingStats => {
  const estimatedMinutes = board.tickets.reduce((sum, ticket) => {
    return sum + (ticket.estimatedMinutes ?? 0);
  }, 0);
  const loggedMinutes = board.tickets.reduce((sum, ticket) => {
    return sum + ticket.timeEntries.reduce((ticketSum, entry) => {
      return ticketSum + entry.durationMinutes;
    }, 0);
  }, 0);

  return {
    estimatedTime: formatMinutesToTimeTrackingDuration(estimatedMinutes),
    loggedTime: formatMinutesToTimeTrackingDuration(loggedMinutes),
    overdueTime: formatMinutesToTimeTrackingDuration(loggedMinutes - estimatedMinutes)
  };
};

export const BoardCard = ({ board }: BoardCardProps): ReactElement => {
  const ticketCounts = getTicketCounts(board);
  const timeTrackingStats = getTimeTrackingStats(board);
  const navigate = useNavigate();
  const boardPath = "/boards/" + board.boardId;
  const boardSettingsPath = boardPath + "/settings";
  const showSettingsButton = canManageBoardSettings(board);

  const openBoard = (): void => {
    void navigate(boardPath);
  };

  return (
    <Paper
      onClick={openBoard}
      onKeyDown={event => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault();
          openBoard();
        }
      }}
      role="link"
      sx={{
        borderRadius: 0.5,
        cursor: "pointer",
        p: 1
      }}
      tabIndex={0}
      variant="outlined"
    >
      <Stack spacing={2}>
        <BoardCardHeader
          board={board}
          onOpenSettings={() => {
            void navigate(boardSettingsPath);
          }}
          showSettingsButton={showSettingsButton}
        />
        <Stack
          direction="row"
          flexWrap="wrap"
          spacing={1.5}
          useFlexGap
        >
          <TicketsInfo ticketCounts={ticketCounts} />
          <TimeTrackingInfo stats={timeTrackingStats} />
          <BoardInfo board={board} />
        </Stack>
      </Stack>
    </Paper>
  );
};
