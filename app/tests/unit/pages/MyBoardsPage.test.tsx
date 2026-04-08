import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { useBoards } from "@hooks/useBoards";
import { MyBoardsPage } from "@pages/MyBoardsPage";
import { renderApp } from "@tests/setup/render";

vi.mock("@hooks/useBoards", () => ({
  useBoards: vi.fn()
}));

vi.mock("@providers/BoardsProvider", () => ({
  BoardsProvider: ({ children }: { children: React.ReactNode }) => children
}));

describe("MyBoardsPage", () => {
  test("renders the empty boards state and opens the create board dialog", async () => {
    const user = userEvent.setup();
    const createBoard = vi.fn().mockResolvedValue(undefined);

    vi.mocked(useBoards).mockReturnValue({
      boards: [],
      boardsError: null,
      createBoard,
      currentParams: {
        active: true,
        page: 1
      },
      isCreatingBoard: false,
      isLoadingBoards: false,
      queryBoards: vi.fn()
    });

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
  });
});
