import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { BoardTicketCard } from "@components/boards/board-page/BoardTicketCard";
import { useBoard } from "@hooks/useBoard";
import { useTickets } from "@hooks/useTickets";
import { useTimeTracking } from "@hooks/useTimeTracking";
import { renderApp } from "@tests/setup/render";

vi.mock("@hooks/useBoard", () => ({
  useBoard: vi.fn()
}));

vi.mock("@hooks/useTickets", () => ({
  useTickets: vi.fn()
}));

vi.mock("@hooks/useTimeTracking", () => ({
  useTimeTracking: vi.fn()
}));

const ticket = {
  acceptanceCriteria: null,
  assignedTo: null,
  assignedToUserId: null,
  board: {
    active: true,
    boardId: "board-1",
    name: "Board"
  },
  boardId: "board-1",
  comments: [],
  commentsCount: 0,
  createdAt: "2026-04-17T10:00:00Z",
  createdBy: null,
  createdByUserId: "user-1",
  description: "Ticket description",
  estimatedMinutes: 30,
  lastModifiedBy: null,
  lastModifiedByUserId: "user-1",
  modifiedAt: "2026-04-17T10:00:00Z",
  name: "Ticket title",
  priority: 2,
  severityId: null,
  severityName: null,
  status: "new",
  ticketId: "ticket-1",
  timeEntries: [],
  trackedMinutes: 15
};

const board = {
  active: true,
  boardId: "board-1",
  createdAt: "2026-04-17T10:00:00Z",
  createdBy: null,
  createdByUserId: "user-1",
  currentUserRole: null,
  description: null,
  lastModifiedByUserId: "user-1",
  membersCount: 1,
  modifiedAt: "2026-04-17T10:00:00Z",
  name: "Board",
  owner: null,
  ownerUserId: "user-1",
  tickets: []
};

const setupCommonMocks = (): { openLogTimeModal: ReturnType<typeof vi.fn> } => {
  const openLogTimeModal = vi.fn();

  vi.mocked(useTickets).mockReturnValue({
    assignedToUserIds: [],
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
    setAssignedToUserIds: vi.fn(),
    setColumnPriorityDirection: vi.fn(),
    setSelectedPriorities: vi.fn(),
    setSelectedSeverityIds: vi.fn(),
    setSearchKeywords: vi.fn(),
    selectedPriorities: [],
    selectedSeverityIds: [],
    tickets: [],
    ticketsCount: 0,
    ticketsError: null,
    transitionTicketState: vi.fn(),
    updateTicket: vi.fn()
  });

  vi.mocked(useTimeTracking).mockReturnValue({
    closeLogTimeModal: vi.fn(),
    isLogTimeModalOpen: false,
    isRegisteringTime: false,
    openLogTimeModal,
    registerTime: vi.fn(),
    selectedTicketId: null,
    setOnRegister: vi.fn()
  });

  return {
    openLogTimeModal
  };
};

describe("BoardTicketCard", () => {
  test("shows log time button and opens modal when board is active", async () => {
    const user = userEvent.setup();
    const { openLogTimeModal } = setupCommonMocks();

    vi.mocked(useBoard).mockReturnValue({
      activateBoard: vi.fn(),
      board: {
        ...board,
        active: true
      },
      boardError: null,
      boardPermissionAccess: {
        canCreate: true,
        canDelete: true,
        canModify: true,
        canRead: true,
        canReassign: false
      },
      canManageBoardSettings: false,
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
      members: [],
      membersError: null,
      removeBoardMember: vi.fn()
    });

    renderApp(<BoardTicketCard onDragEnd={vi.fn()} onDragStart={vi.fn()} ticket={ticket} />);

    await user.click(screen.getByRole("button", { name: "Log time" }));

    expect(openLogTimeModal).toHaveBeenCalledWith("ticket-1");
  });

  test("hides log time button when board is inactive", () => {
    setupCommonMocks();

    vi.mocked(useBoard).mockReturnValue({
      activateBoard: vi.fn(),
      board: {
        ...board,
        active: false
      },
      boardError: null,
      boardPermissionAccess: {
        canCreate: true,
        canDelete: true,
        canModify: true,
        canRead: true,
        canReassign: false
      },
      canManageBoardSettings: false,
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
      members: [],
      membersError: null,
      removeBoardMember: vi.fn()
    });

    renderApp(<BoardTicketCard onDragEnd={vi.fn()} onDragStart={vi.fn()} ticket={ticket} />);

    expect(screen.queryByRole("button", { name: "Log time" })).not.toBeInTheDocument();
  });

  test("disables interactive actions when disableActions is true", () => {
    setupCommonMocks();

    vi.mocked(useBoard).mockReturnValue({
      activateBoard: vi.fn(),
      board: {
        ...board,
        active: true
      },
      boardError: null,
      boardPermissionAccess: {
        canCreate: true,
        canDelete: true,
        canModify: true,
        canRead: true,
        canReassign: true
      },
      canManageBoardSettings: false,
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
      members: [],
      membersError: null,
      removeBoardMember: vi.fn()
    });

    renderApp(
      <BoardTicketCard
        disableActions
        onDragEnd={vi.fn()}
        onDragStart={vi.fn()}
        ticket={ticket}
      />
    );

    expect(screen.queryByRole("button", { name: "Log time" })).not.toBeInTheDocument();
  });
});
