import type { DragEvent, ReactElement } from "react";

import { BoardTicketCardView } from "@components/boards/board-page/BoardTicketCardView";
import { useBoard } from "@hooks/useBoard";
import { useTickets } from "@hooks/useTickets";
import { useTimeTracking } from "@hooks/useTimeTracking";
import type { Ticket } from "../../../domain/ticket/graphql";

interface BoardTicketCardProps {
  disableActions?: boolean | undefined;
  onDragEnd: () => void;
  onDragStart: (event: DragEvent<HTMLDivElement>, ticketId: string) => void;
  ticket: Ticket;
}

export const BoardTicketCard = ({
  disableActions = false,
  onDragEnd,
  onDragStart,
  ticket
}: BoardTicketCardProps): ReactElement => {
  const { board, boardPermissionAccess, members } = useBoard();
  const { reassignTicket, transitionTicketState } = useTickets();
  const { openLogTimeModal } = useTimeTracking();

  return (
    <BoardTicketCardView
      canReassign={boardPermissionAccess.canReassign}
      canRegisterTime={board?.active === true}
      disableActions={disableActions}
      members={members}
      onDragEnd={onDragEnd}
      onDragStart={onDragStart}
      onLogTime={openLogTimeModal}
      onReassign={(ticketId, assignedToUserId) => {
        void reassignTicket(ticketId, assignedToUserId);
      }}
      onStateChange={(ticketId, status) => {
        void transitionTicketState(ticketId, status);
      }}
      ticket={ticket}
    />
  );
};
