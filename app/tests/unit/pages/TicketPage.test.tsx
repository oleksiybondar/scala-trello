import { screen } from "@testing-library/react";

import { TicketPage } from "@pages/TicketPage";
import { renderApp } from "@tests/setup/render";

describe("TicketPage", () => {
  test("shows validation message when ticket id is missing", () => {
    renderApp(<TicketPage />);

    expect(screen.getByRole("heading", { name: "Ticket details" })).toBeInTheDocument();
    expect(screen.getByText("Ticket id is missing.")).toBeInTheDocument();
  });
});
