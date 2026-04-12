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
      tickets: [],
      ticketsCount: 5
    });

    renderApp(<BoardPageToolbar />);

    expect(screen.getByText("5 tickets")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "New ticket" }));

    expect(screen.getByRole("heading", { name: "Create ticket" })).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "Ticket title" })).toBeInTheDocument();
  });
});
