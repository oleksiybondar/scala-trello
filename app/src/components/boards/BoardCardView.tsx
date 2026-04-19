import type { KeyboardEvent, ReactElement } from "react";

import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";

import { BoardCardHeader } from "@components/boards/BoardCardHeader";
import { BoardInfo } from "@components/boards/BoardInfo";
import { TicketsInfo } from "@components/boards/TicketsInfo";
import { TimeTrackingInfo } from "@components/boards/TimeTrackingInfo";
import type { TicketStateCounts } from "@components/boards/tickets-info/types";
import type { TimeVelocityData } from "@components/charts/TimeVelocityChart";
import type { Board } from "../../domain/board/graphql";

interface BoardCardViewProps {
  board: Board;
  onOpenBoard?: (() => void) | undefined;
  onOpenSettings?: (() => void) | undefined;
  showSettingsButton?: boolean | undefined;
  ticketCounts: TicketStateCounts;
  timeTrackingData: TimeVelocityData;
}

const noopOpenSettings = (): void => undefined;

export const BoardCardView = ({
  board,
  onOpenBoard,
  onOpenSettings,
  showSettingsButton = false,
  ticketCounts,
  timeTrackingData
}: BoardCardViewProps): ReactElement => {
  const isInteractive = onOpenBoard !== undefined;

  const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>): void => {
    if (!isInteractive) {
      return;
    }

    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      onOpenBoard();
    }
  };

  return (
    <Paper
      onClick={onOpenBoard}
      onKeyDown={handleKeyDown}
      role={isInteractive ? "link" : undefined}
      sx={{
        borderRadius: 0.5,
        cursor: isInteractive ? "pointer" : "default",
        p: 1
      }}
      tabIndex={isInteractive ? 0 : -1}
      variant="outlined"
    >
      <Stack spacing={2}>
        <BoardCardHeader
          board={board}
          onOpenSettings={onOpenSettings ?? noopOpenSettings}
          showSettingsButton={showSettingsButton}
        />
        <Stack
          direction="row"
          flexWrap="wrap"
          spacing={1.5}
          useFlexGap
        >
          <TicketsInfo ticketCounts={ticketCounts} />
          <TimeTrackingInfo data={timeTrackingData} />
          <BoardInfo board={board} />
        </Stack>
      </Stack>
    </Paper>
  );
};
