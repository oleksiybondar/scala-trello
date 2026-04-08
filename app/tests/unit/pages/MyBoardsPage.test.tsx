import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { MyBoardsPage } from "@pages/MyBoardsPage";
import { renderApp } from "@tests/setup/render";

describe("MyBoardsPage", () => {
  test("renders the empty boards state and opens the create board dialog", async () => {
    const user = userEvent.setup();

    renderApp(<MyBoardsPage />);

    expect(
      screen.getByRole("heading", { name: "My boards" })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("heading", { name: "You don't have any boards yet." })
    ).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Create board" }));

    expect(
      screen.getByRole("dialog", { name: "Create board" })
    ).toBeInTheDocument();
    expect(
      screen.getByText(
        "This dialog is intentionally stubbed for the current iteration."
      )
    ).toBeInTheDocument();
  });
});
