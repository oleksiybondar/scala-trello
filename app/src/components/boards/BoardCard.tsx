import type { ReactElement } from "react";

import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import { useNavigate } from "react-router-dom";

import { BoardCardHeader } from "@components/boards/BoardCardHeader";
import { BoardInfo } from "@components/boards/BoardInfo";
import { TicketsInfo } from "@components/boards/TicketsInfo";
import { TimeTrackingInfo } from "@components/boards/TimeTrackingInfo";
import type { Board } from "@models/board";
import type { TicketStateCounts } from "@components/boards/tickets-info/types";
import type { TimeTrackingStats } from "@components/boards/time-tracking-info/types";

interface BoardCardProps {
  board: Board;
}

const getStubTicketCounts = (board: Board): TicketStateCounts => {
  const seed = board.name.length + board.membersCount * 3;
  const base = Math.max(2, board.membersCount);

  return {
    code_review: 1 + ((seed + 3) % 3),
    done: base + ((seed + 1) % 6),
    in_progress: base + ((seed + 2) % 5),
    in_testing: 1 + ((seed + 4) % 2),
    new: base + (seed % 4)
  };
};

const getStubTimeTrackingStats = (board: Board): TimeTrackingStats => {
  const seed = board.name.length + board.membersCount * 5;
  const estimated = Math.max(12, board.membersCount * 8 + seed);
  const logged = Math.max(8, estimated - 6 + (seed % 12));
  const overdue = Math.max(0, logged - estimated);

  const formatDuration = (hours: number): string => {
    const totalMinutes = hours * 60;
    const resolvedHours = Math.floor(totalMinutes / 60);
    const resolvedMinutes = totalMinutes % 60;

    return String(resolvedHours) + "h:" + String(resolvedMinutes).padStart(2, "0") + "m";
  };

  return {
    estimatedTime: formatDuration(estimated),
    loggedTime: formatDuration(logged),
    overdueTime: formatDuration(overdue)
  };
};

export const BoardCard = ({ board }: BoardCardProps): ReactElement => {
  const ticketCounts = getStubTicketCounts(board);
  const timeTrackingStats = getStubTimeTrackingStats(board);
  const navigate = useNavigate();
  const boardPath = "/boards/" + board.boardId;
  const boardSettingsPath = boardPath + "/settings";

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
