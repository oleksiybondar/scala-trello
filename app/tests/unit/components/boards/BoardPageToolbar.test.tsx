import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { BoardPageToolbar } from "@components/boards/board-page/BoardPageToolbar";
import { useBoard } from "@hooks/useBoard";
import { useSeverities } from "@hooks/useSeverities";
import { useTickets } from "@hooks/useTickets";
import { renderApp } from "@tests/setup/render";

vi.mock("@hooks/useBoard", () => ({
  useBoard: vi.fn()
}));

vi.mock("@hooks/useTickets", () => ({
  useTickets: vi.fn()
}));

vi.mock("@hooks/useSeverities", () => ({
  useSeverities: vi.fn()
}));

vi.mock("@components/tickets/CreateTicketDialog", () => ({
  CreateTicketDialog: ({ open }: { open: boolean }) => {
    return open ? <div>Create ticket dialog</div> : null;
  }
}));

describe("BoardPageToolbar", () => {
  const mockBoard = () => {
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
        },
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
            firstName: "Bob",
            lastName: "Smith",
            userId: "user-2"
          },
          userId: "user-2"
        }
      ],
      membersError: null,
      removeBoardMember: vi.fn()
    });
  };

  const mockSeverities = () => {
    vi.mocked(useSeverities).mockReturnValue({
      hasLoadedSeverities: true,
      isLoadingSeverities: false,
      loadSeverities: vi.fn(),
      severities: [
        {
          description: "Highest impact",
          name: "critical",
          severityId: "1"
        },
        {
          description: "Medium impact",
          name: "major",
          severityId: "2"
        },
        {
          description: "Lower impact",
          name: "minor",
          severityId: "3"
        }
      ],
      severitiesError: null
    });
  };

  test("renders filtered ticket count, filter controls, and opens the create ticket dialog", async () => {
    const user = userEvent.setup();
    const setColumnPriorityDirection = vi.fn();
    const setAssignedToUserIds = vi.fn();
    const setSelectedPriorities = vi.fn();
    const setSelectedSeverityIds = vi.fn();
    const setSearchKeywords = vi.fn();
    mockBoard();

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
      columnPriorityDirections: {
        code_review: "high_to_low",
        done: "high_to_low",
        in_progress: "high_to_low",
        in_testing: "high_to_low",
        new: "high_to_low"
      },
      reassignTicket: vi.fn(),
      reloadTickets: vi.fn(),
      resetFilters: vi.fn(),
      searchKeywords: "",
      setColumnPriorityDirection,
      setAssignedToUserIds,
      setSelectedPriorities,
      setSelectedSeverityIds,
      setSearchKeywords,
      selectedPriorities: [],
      selectedSeverityIds: [],
      tickets: [],
      ticketsCount: 4,
      ticketsError: null,
      transitionTicketState: vi.fn(),
      updateTicket: vi.fn()
    });
    mockSeverities();

    renderApp(<BoardPageToolbar />);

    expect(screen.getByText("2/4 tickets")).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "Search tickets" })).toBeInTheDocument();
    expect(screen.getByRole("combobox", { name: "Assignees" })).toBeInTheDocument();
    expect(screen.getByRole("combobox", { name: "Severity" })).toBeInTheDocument();
    expect(screen.getByRole("combobox", { name: "Priority" })).toBeInTheDocument();

    await user.type(screen.getByRole("textbox", { name: "Search tickets" }), "auth");

    expect(setSearchKeywords).toHaveBeenCalled();

    await user.click(screen.getByRole("button", { name: "New ticket" }));

    expect(screen.getByText("Create ticket dialog")).toBeInTheDocument();
  });

  test("shows one selected label and many selected count in multi-select filters", () => {
    const setColumnPriorityDirection = vi.fn();
    const setAssignedToUserIds = vi.fn();
    const setSelectedPriorities = vi.fn();
    const setSelectedSeverityIds = vi.fn();
    const setSearchKeywords = vi.fn();

    mockBoard();
    mockSeverities();
    vi.mocked(useTickets).mockReturnValue({
      assignedToUserIds: ["user-1"],
      codeReviewTickets: [],
      createTicket: vi.fn(),
      doneTickets: [],
      inProgressTickets: [],
      inTestingTickets: [],
      isCreatingTicket: false,
      isReassigningTicket: false,
      isReloadingTickets: false,
      isTransitioningTicketState: false,
      newTickets: [],
      columnPriorityDirections: {
        code_review: "high_to_low",
        done: "high_to_low",
        in_progress: "high_to_low",
        in_testing: "high_to_low",
        new: "high_to_low"
      },
      reassignTicket: vi.fn(),
      reloadTickets: vi.fn(),
      resetFilters: vi.fn(),
      searchKeywords: "",
      setColumnPriorityDirection,
      setAssignedToUserIds,
      setSelectedPriorities,
      setSelectedSeverityIds,
      setSearchKeywords,
      selectedPriorities: [1, 2],
      selectedSeverityIds: ["1"],
      tickets: [],
      ticketsCount: 0,
      ticketsError: null,
      transitionTicketState: vi.fn(),
      updateTicket: vi.fn()
    });

    renderApp(<BoardPageToolbar />);

    expect(screen.getByRole("combobox", { name: "Assignees" })).toHaveTextContent("Alice Example");
    expect(screen.getByRole("combobox", { name: "Severity" })).toHaveTextContent("Critical");
    expect(screen.getByRole("combobox", { name: "Priority" })).toHaveTextContent("2 selected");
  });
});
