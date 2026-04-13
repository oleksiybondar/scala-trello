import type { PropsWithChildren, ReactElement } from "react";
import { useState } from "react";

import { TimeTrackingModal } from "@components/boards/board-page/TimeTrackingModal";
import { TimeTrackingContext } from "@contexts/time-tracking-context";

export const TimeTrackingProvider = ({
  children
}: PropsWithChildren): ReactElement => {
  const [selectedTicketId, setSelectedTicketId] = useState<string | null>(null);

  const openLogTimeModal = (ticketId: string): void => {
    setSelectedTicketId(ticketId);
  };

  const closeLogTimeModal = (): void => {
    setSelectedTicketId(null);
  };

  return (
    <TimeTrackingContext.Provider
      value={{
        closeLogTimeModal,
        isLogTimeModalOpen: selectedTicketId !== null,
        openLogTimeModal,
        selectedTicketId
      }}
    >
      {children}
      <TimeTrackingModal
        isOpen={selectedTicketId !== null}
        onClose={closeLogTimeModal}
        ticketId={selectedTicketId}
      />
    </TimeTrackingContext.Provider>
  );
};
