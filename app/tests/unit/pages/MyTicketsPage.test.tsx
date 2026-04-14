import { screen } from "@testing-library/react";

import { MyTicketsPage } from "@pages/MyTicketsPage";
import { renderApp } from "@tests/setup/render";

describe("MyTicketsPage", () => {
  test("renders the my tickets stub content", () => {
    renderApp(<MyTicketsPage />);

    expect(screen.getByRole("heading", { name: "My tickets" })).toBeInTheDocument();
    expect(
      screen.getByText("This page is a stub. Ticket-focused personal view is coming next.")
    ).toBeInTheDocument();
  });
});
