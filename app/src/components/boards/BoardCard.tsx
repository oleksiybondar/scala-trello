import type { ReactElement } from "react";

import { useNavigate } from "react-router-dom";

import { BoardCardView } from "@components/boards/BoardCardView";
import { mapUiTicketStatusToStateKey } from "@helpers/uiTicketStatus";
import { canManageBoardSettings } from "../../domain/board/boardPermissions";
import type { Board } from "../../domain/board/graphql";
import type { TicketStateCounts } from "@components/boards/tickets-info/types";
import type { TimeVelocityData } from "@components/charts/TimeVelocityChart";

interface BoardCardProps {
  board: Board;
}

interface BoardTimeTrackingSummary {
  actualTime?: number[] | null;
  estimatedTime?: number | null;
  totalLoggedTime?: number | null;
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

const getTicketCounts = (board: Board): TicketStateCounts => {
  return board.tickets.reduce((counts, ticket) => {
    const stateKey = mapUiTicketStatusToStateKey(ticket.status);

    if (stateKey === null) {
      return counts;
    }

    return {
      ...counts,
      [stateKey]: counts[stateKey] + 1
    };
  }, createEmptyTicketCounts());
};

const getTimeTrackingData = (board: Board): TimeVelocityData => {
  const summary = board as Board & BoardTimeTrackingSummary;
  const estimatedFromTickets = board.tickets.reduce((sum, ticket) => {
    return sum + (ticket.estimatedMinutes ?? 0);
  }, 0);
  const loggedFromTickets = board.tickets.reduce((sum, ticket) => {
    return sum + Math.max(0, ticket.trackedMinutes);
  }, 0);
  const entrySeriesFromTickets = board.tickets
    .flatMap(ticket => {
      return ticket.timeEntries;
    })
    .sort((left, right) => {
      return new Date(left.loggedAt).getTime() - new Date(right.loggedAt).getTime();
    })
    .reduce<number[]>((series, entry) => {
      const previous = series[series.length - 1] ?? 0;

      return [...series, previous + Math.max(0, entry.durationMinutes)];
    }, []);
  const estimatedMinutes = Math.max(0, summary.estimatedTime ?? estimatedFromTickets);
  const actualSeriesMinutes =
    Array.isArray(summary.actualTime) && summary.actualTime.length > 0
      ? summary.actualTime.map(value => Math.max(0, value))
      : entrySeriesFromTickets.length > 0
        ? entrySeriesFromTickets
        : [Math.max(0, summary.totalLoggedTime ?? loggedFromTickets)];

  return {
    actualSeriesMinutes,
    estimatedMinutes
  };
};

export const BoardCard = ({ board }: BoardCardProps): ReactElement => {
  const ticketCounts = getTicketCounts(board);
  const timeTrackingData = getTimeTrackingData(board);
  const navigate = useNavigate();
  const boardPath = "/boards/" + board.boardId;
  const boardSettingsPath = boardPath + "/settings";
  const showSettingsButton = canManageBoardSettings(board);

  const openBoard = (): void => {
    void navigate(boardPath);
  };

  return (
    <BoardCardView
      board={board}
      onOpenBoard={openBoard}
      onOpenSettings={() => {
        void navigate(boardSettingsPath);
      }}
      showSettingsButton={showSettingsButton}
      ticketCounts={ticketCounts}
      timeTrackingData={timeTrackingData}
    />
  );
};
