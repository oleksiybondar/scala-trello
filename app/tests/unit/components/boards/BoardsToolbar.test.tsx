import { fireEvent, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { BoardsToolbar } from "@components/boards/boards-toolbar/BoardsToolbar";
import { useBoards } from "@hooks/useBoards";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { renderApp } from "@tests/setup/render";

vi.mock("@hooks/useBoards", () => ({
  useBoards: vi.fn()
}));

vi.mock("@hooks/useCurrentUser", () => ({
  useCurrentUser: vi.fn()
}));

describe("BoardsToolbar", () => {
  test("renders owner options and dispatches search, owner, and create actions", async () => {
    const user = userEvent.setup();
    const onCreateBoard = vi.fn();
    const queryBoards = vi.fn();

    vi.mocked(useCurrentUser).mockReturnValue({
      changeEmail: vi.fn(),
      changePassword: vi.fn(),
      changeUsername: vi.fn(),
      currentUser: null,
      refreshCurrentUser: vi.fn(),
      setCurrentUser: vi.fn(),
      updateAvatar: vi.fn(),
      updateProfile: vi.fn(),
      userId: "user-me"
    });

    vi.mocked(useBoards).mockReturnValue({
      boards: [],
      canLoadMoreBoards: false,
      boardsError: null,
      createBoard: vi.fn(),
      currentParams: {
        keyword: undefined,
        owner: undefined,
        page: 1,
        showInactive: false
      },
      isCreatingBoard: false,
      isLoadingBoards: false,
      isLoadingMoreBoards: false,
      loadNextBoardsPage: vi.fn(),
      ownerOptions: [
        {
          label: "Zelda Owner",
          ownerUserId: "user-z"
        },
        {
          label: "Alice Owner",
          ownerUserId: "user-a"
        }
      ],
      queryBoards
    });

    renderApp(<BoardsToolbar onCreateBoard={onCreateBoard} />);

    fireEvent.change(screen.getByRole("textbox", { name: "Search boards" }), {
      target: {
        value: "platform"
      }
    });

    expect(queryBoards).toHaveBeenLastCalledWith({
      keyword: "platform",
      page: 1
    });

    const ownerInput = screen.getByRole("combobox", { name: "Owned by" });

    await user.click(ownerInput);

    const listbox = await screen.findByRole("listbox");
    const options = within(listbox).getAllByRole("option");

    expect(options.map(option => option.textContent)).toEqual([
      "Any",
      "Me",
      "Zelda Owner",
      "Alice Owner"
    ]);

    await user.click(within(listbox).getByRole("option", { name: "Alice Owner" }));

    expect(queryBoards).toHaveBeenLastCalledWith({
      owner: "user-a",
      page: 1
    });

    await user.click(screen.getByRole("switch", { name: "Show inactive" }));

    expect(queryBoards).toHaveBeenLastCalledWith({
      page: 1,
      showInactive: true
    });

    await user.click(screen.getByRole("button", { name: "New board" }));

    expect(onCreateBoard).toHaveBeenCalledTimes(1);
  });
});
