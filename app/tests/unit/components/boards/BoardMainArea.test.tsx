import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { BoardMainArea } from "@components/boards/board-page/BoardMainArea";
import { useTickets } from "@hooks/useTickets";
import { renderApp } from "@tests/setup/render";

vi.mock("@hooks/useTickets", () => ({
  useTickets: vi.fn()
}));

describe("BoardMainArea", () => {
  test("toggles priority direction for an individual column", async () => {
    const user = userEvent.setup();
    const setColumnPriorityDirection = vi.fn();

    vi.mocked(useTickets).mockReturnValue({
      assignedToUserIds: [],
      codeReviewTickets: [],
      columnPriorityDirections: {
        code_review: "high_to_low",
        done: "high_to_low",
        in_progress: "high_to_low",
        in_testing: "high_to_low",
        new: "high_to_low"
      },
      createTicket: vi.fn(),
      doneTickets: [],
      inProgressTickets: [],
      inTestingTickets: [],
      isCreatingTicket: false,
      isReassigningTicket: false,
      isReloadingTickets: false,
      isTransitioningTicketState: false,
      newTickets: [],
      reassignTicket: vi.fn(),
      reloadTickets: vi.fn(),
      resetFilters: vi.fn(),
      searchKeywords: "",
      setAssignedToUserIds: vi.fn(),
      setColumnPriorityDirection,
      setSelectedPriorities: vi.fn(),
      setSelectedSeverityIds: vi.fn(),
      setSearchKeywords: vi.fn(),
      selectedPriorities: [],
      selectedSeverityIds: [],
      tickets: [],
      ticketsCount: 0,
      ticketsError: null,
      transitionTicketState: vi.fn(),
      updateTicket: vi.fn()
    });

    renderApp(<BoardMainArea />);

    await user.click(screen.getByRole("button", { name: "Toggle priority direction for New" }));

    expect(setColumnPriorityDirection).toHaveBeenCalledWith("new", "low_to_high");
  });
});
