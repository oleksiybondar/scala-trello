import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { useBoards } from "@hooks/useBoards";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { MyBoardsPage } from "@pages/MyBoardsPage";
import { renderApp } from "@tests/setup/render";

vi.mock("@hooks/useBoards", () => ({
  useBoards: vi.fn()
}));

vi.mock("@hooks/useCurrentUser", () => ({
  useCurrentUser: vi.fn()
}));

vi.mock("@providers/BoardsProvider", () => ({
  BoardsProvider: ({ children }: { children: React.ReactNode }) => children
}));

describe("MyBoardsPage", () => {
  test("renders the empty boards state and opens the create board dialog", async () => {
    const user = userEvent.setup();
    const createBoard = vi.fn().mockResolvedValue(undefined);
    const queryBoards = vi.fn();

    vi.mocked(useCurrentUser).mockReturnValue({
      currentUser: null,
      refreshCurrentUser: vi.fn(),
      setCurrentUser: vi.fn(),
      userId: null
    });

    vi.mocked(useBoards).mockReturnValue({
      boards: [],
      boardsError: null,
      createBoard,
      currentParams: {
        active: true,
        keyword: undefined,
        owner: undefined,
        page: 1
      },
      isCreatingBoard: false,
      isLoadingBoards: false,
      ownerOptions: [],
      queryBoards
    });

    renderApp(<MyBoardsPage />);

    expect(
      screen.getByRole("textbox", { name: "Search boards" })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("combobox", { name: "Owned by" })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("heading", { name: "You don't have any boards yet." })
    ).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "New board" }));

    expect(
      screen.getByRole("dialog", { name: "Create board" })
    ).toBeInTheDocument();

    const submitButton = screen.getByRole("button", { name: "Create board" });

    expect(submitButton).toBeDisabled();

    await user.type(screen.getByRole("textbox", { name: "Board name" }), "API Sprint");

    expect(submitButton).toBeEnabled();
  });

  test("renders not found state when search is active and no boards match", () => {
    vi.mocked(useCurrentUser).mockReturnValue({
      currentUser: null,
      refreshCurrentUser: vi.fn(),
      setCurrentUser: vi.fn(),
      userId: null
    });

    vi.mocked(useBoards).mockReturnValue({
      boards: [],
      boardsError: null,
      createBoard: vi.fn(),
      currentParams: {
        active: true,
        keyword: "sprint",
        owner: undefined,
        page: 1
      },
      isCreatingBoard: false,
      isLoadingBoards: false,
      ownerOptions: [],
      queryBoards: vi.fn()
    });

    renderApp(<MyBoardsPage />);

    expect(
      screen.getByRole("heading", { name: "No boards found." })
    ).toBeInTheDocument();
  });
});
