import { screen } from "@testing-library/react";

import { MyTicketsPage } from "@pages/MyTicketsPage";
import { renderApp } from "@tests/setup/render";

describe("MyTicketsPage", () => {
  test("renders my tickets table empty state", () => {
    renderApp(<MyTicketsPage />);

    expect(screen.getByRole("heading", { name: "My tickets" })).toBeInTheDocument();
    expect(screen.getByText("No tickets found.")).toBeInTheDocument();
  });
});
