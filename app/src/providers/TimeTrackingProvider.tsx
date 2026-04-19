import type { PropsWithChildren, ReactElement } from "react";
import { useEffect, useRef, useState } from "react";

import { TimeTrackingModal } from "@components/boards/board-page/TimeTrackingModal";
import { TimeTrackingContext } from "@contexts/time-tracking-context";
import type { RegisterTimeTrackingOptions } from "@contexts/time-tracking-context";
import type { TimeTrackingOnRegister } from "@contexts/time-tracking-context";
import type { RegisterTimeTrackingEntryInput } from "../domain/time-tracking/useTimeTrackingEntryService";
import { useTimeTrackingEntryService } from "../domain/time-tracking/useTimeTrackingEntryService";

interface TimeTrackingProviderProps extends PropsWithChildren {
  onRegister?: TimeTrackingOnRegister;
}

export const TimeTrackingProvider = ({
  children,
  onRegister
}: TimeTrackingProviderProps): ReactElement => {
  const [selectedTicketId, setSelectedTicketId] = useState<string | null>(null);
  const onRegisterRef = useRef(onRegister);
  const { isRegisteringTime, registerTimeEntry } = useTimeTrackingEntryService();

  useEffect(() => {
    onRegisterRef.current = onRegister;
  }, [onRegister]);

  const openLogTimeModal = (ticketId: string): void => {
    setSelectedTicketId(ticketId);
  };

  const closeLogTimeModal = (): void => {
    setSelectedTicketId(null);
  };

  const setOnRegister = (nextOnRegister?: TimeTrackingOnRegister): void => {
    onRegisterRef.current = nextOnRegister;
  };

  const registerTime = async (
    input: RegisterTimeTrackingEntryInput,
    options?: RegisterTimeTrackingOptions
  ): ReturnType<typeof registerTimeEntry> => {
    const createdEntry = await registerTimeEntry(input);
    const onRegisterCallback = options?.onRegister ?? onRegisterRef.current;

    if (onRegisterCallback !== undefined) {
      await onRegisterCallback(createdEntry);
    }

    return createdEntry;
  };

  return (
    <TimeTrackingContext.Provider
      value={{
        closeLogTimeModal,
        isLogTimeModalOpen: selectedTicketId !== null,
        isRegisteringTime,
        openLogTimeModal,
        registerTime,
        selectedTicketId,
        setOnRegister
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
