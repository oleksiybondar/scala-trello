import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { BoardPageToolbar } from "@components/boards/board-page/BoardPageToolbar";
import { useBoard } from "@hooks/useBoard";
import { useTickets } from "@hooks/useTickets";
import { renderApp } from "@tests/setup/render";

vi.mock("@hooks/useBoard", () => ({
  useBoard: vi.fn()
}));

vi.mock("@hooks/useTickets", () => ({
  useTickets: vi.fn()
}));

describe("BoardPageToolbar", () => {
  test("renders filtered ticket count, filter controls, and opens the create ticket dialog", async () => {
    const user = userEvent.setup();
    const setAssignedToUserIds = vi.fn();
    const setSearchKeywords = vi.fn();

    vi.mocked(useBoard).mockReturnValue({
      activateBoard: vi.fn(),
      board: null,
      boardError: null,
      boardPermissionAccess: {
        canCreate: true,
        canDelete: true,
        canModify: true,
        canRead: true,
        canReassign: true
      },
      canManageBoardSettings: true,
      changeBoardDescription: vi.fn(),
      changeBoardMemberRole: vi.fn(),
      changeBoardOwnership: vi.fn(),
      changeBoardTitle: vi.fn(),
      deactivateBoard: vi.fn(),
      inviteBoardMember: vi.fn(),
      isInvitingBoardMember: false,
      isLoadingBoard: false,
      isLoadingMembers: false,
      isRemovingBoardMember: false,
      isUpdatingBoardDescription: false,
      isUpdatingBoardMemberRole: false,
      isUpdatingBoardOwnership: false,
      isUpdatingBoardStatus: false,
      isUpdatingBoardTitle: false,
      members: [
        {
          boardId: "board-1",
          createdAt: "2026-04-01T00:00:00Z",
          role: {
            description: null,
            permissions: [],
            roleId: "role-1",
            roleName: "member"
          },
          user: {
            avatarUrl: null,
            firstName: "Alice",
            lastName: "Example",
            userId: "user-1"
          },
          userId: "user-1"
        }
      ],
      membersError: null,
      removeBoardMember: vi.fn()
    });

    vi.mocked(useTickets).mockReturnValue({
      assignedToUserIds: [],
      codeReviewTickets: [{}] as never[],
      createTicket: vi.fn(),
      doneTickets: [],
      inProgressTickets: [{}] as never[],
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
      setAssignedToUserIds,
      setSearchKeywords,
      tickets: [],
      ticketsCount: 4,
      ticketsError: null,
      transitionTicketState: vi.fn(),
      updateTicket: vi.fn()
    });

    renderApp(<BoardPageToolbar />);

    expect(screen.getByText("2/4 tickets")).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "Search tickets" })).toBeInTheDocument();
    expect(screen.getByRole("combobox", { name: "Assignees" })).toBeInTheDocument();

    await user.type(screen.getByRole("textbox", { name: "Search tickets" }), "auth");

    expect(setSearchKeywords).toHaveBeenCalled();

    await user.click(screen.getByRole("button", { name: "New ticket" }));

    expect(screen.getByRole("heading", { name: "Create ticket" })).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "Ticket title" })).toBeInTheDocument();
  });
});
