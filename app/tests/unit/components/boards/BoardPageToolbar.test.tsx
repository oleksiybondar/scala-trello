import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { BoardPageToolbar } from "@components/boards/board-page/BoardPageToolbar";
import { useTickets } from "@hooks/useTickets";
import { renderApp } from "@tests/setup/render";

vi.mock("@hooks/useTickets", () => ({
  useTickets: vi.fn()
}));

describe("BoardPageToolbar", () => {
  test("renders memoized ticket count and opens the create ticket dialog", async () => {
    const user = userEvent.setup();

    vi.mocked(useTickets).mockReturnValue({
      assignedToUserIds: [],
      codeReviewTickets: [],
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
      setSearchKeywords: vi.fn(),
      tickets: [],
      ticketsCount: 5,
      ticketsError: null,
      transitionTicketState: vi.fn(),
      updateTicket: vi.fn()
    });

    renderApp(<BoardPageToolbar />);

    expect(screen.getByText("5 tickets")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "New ticket" }));

    expect(screen.getByRole("heading", { name: "Create ticket" })).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "Ticket title" })).toBeInTheDocument();
  });
});
