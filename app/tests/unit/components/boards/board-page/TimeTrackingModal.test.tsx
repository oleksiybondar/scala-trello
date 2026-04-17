import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { TimeTrackingModal } from "@components/boards/board-page/TimeTrackingModal";
import { useActivities } from "@hooks/useActivities";
import { useTimeTracking } from "@hooks/useTimeTracking";
import { renderApp } from "@tests/setup/render";

vi.mock("@hooks/useActivities", () => ({
  useActivities: vi.fn()
}));

vi.mock("@hooks/useTimeTracking", () => ({
  useTimeTracking: vi.fn()
}));

describe("TimeTrackingModal", () => {
  test("loads activities when opened", async () => {
    const loadActivities = vi.fn().mockResolvedValue(undefined);

    vi.mocked(useActivities).mockReturnValue({
      activities: [],
      activitiesError: null,
      hasLoadedActivities: false,
      isLoadingActivities: false,
      loadActivities
    });
    vi.mocked(useTimeTracking).mockReturnValue({
      closeLogTimeModal: vi.fn(),
      isLogTimeModalOpen: true,
      isRegisteringTime: false,
      openLogTimeModal: vi.fn(),
      registerTime: vi.fn(),
      selectedTicketId: "ticket-1",
      setOnRegister: vi.fn()
    });

    renderApp(<TimeTrackingModal isOpen onClose={vi.fn()} ticketId="ticket-1" />);

    await waitFor(() => {
      expect(loadActivities).toHaveBeenCalledTimes(1);
    });
  });

  test("submits a new time entry and closes on success", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();
    const registerTime = vi.fn().mockResolvedValue({
      entryId: "entry-1"
    });

    vi.mocked(useActivities).mockReturnValue({
      activities: [
        {
          activityId: "1",
          code: "DEV",
          description: null,
          name: "Development"
        }
      ],
      activitiesError: null,
      hasLoadedActivities: true,
      isLoadingActivities: false,
      loadActivities: vi.fn()
    });
    vi.mocked(useTimeTracking).mockReturnValue({
      closeLogTimeModal: vi.fn(),
      isLogTimeModalOpen: true,
      isRegisteringTime: false,
      openLogTimeModal: vi.fn(),
      registerTime,
      selectedTicketId: "ticket-1",
      setOnRegister: vi.fn()
    });

    renderApp(<TimeTrackingModal isOpen onClose={onClose} ticketId="ticket-1" />);

    await user.type(screen.getByRole("textbox", { name: "Duration (HH:MM)" }), "01:30");
    await user.click(screen.getByRole("button", { name: "Register time" }));

    await waitFor(() => {
      expect(registerTime).toHaveBeenCalledTimes(1);
    });

    const firstCall = registerTime.mock.calls[0]?.[0] as Record<string, unknown>;

    expect(firstCall.activityId).toBe(1);
    expect(firstCall.durationMinutes).toBe(90);
    expect(firstCall.ticketId).toBe("ticket-1");
    expect(typeof firstCall.loggedAt).toBe("string");
    expect(Number.isNaN(Date.parse(String(firstCall.loggedAt)))).toBe(false);
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
