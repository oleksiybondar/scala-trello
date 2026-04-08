import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { resetDashboardStore } from "@features/board/boardsApi";
import { MyBoardsPage } from "@pages/MyBoardsPage";
import { renderApp } from "@tests/setup/render";

describe("MyBoardsPage", () => {
  afterEach(() => {
    resetDashboardStore();
  });

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

    const submitButton = screen.getByRole("button", { name: "Create board" });

    expect(submitButton).toBeDisabled();

    await user.type(screen.getByRole("textbox", { name: "Board name" }), "API Sprint");

    expect(submitButton).toBeEnabled();

    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: "API Sprint" })
      ).toBeInTheDocument();
    });
  });
});
