import { screen } from "@testing-library/react";

import { MyTicketsTable } from "@components/tickets/my-tickets/MyTicketsTable";
import { renderApp } from "@tests/setup/render";
import type { Ticket } from "../../../../../src/domain/ticket/graphql";

const createTicket = (overrides: Partial<Ticket>): Ticket => {
  return {
    acceptanceCriteria: null,
    assignedTo: null,
    assignedToUserId: null,
    board: null,
    boardId: "board-1",
    comments: [],
    commentsCount: 0,
    createdAt: "2026-04-01T00:00:00Z",
    createdBy: null,
    createdByUserId: "user-1",
    description: null,
    estimatedMinutes: null,
    lastModifiedBy: null,
    lastModifiedByUserId: "user-1",
    modifiedAt: "2026-04-01T00:00:00Z",
    name: "Ticket",
    priority: null,
    severityId: null,
    severityName: null,
    status: "new",
    ticketId: "ticket-1",
    timeEntries: [],
    trackedMinutes: 0,
    ...overrides
  };
};

describe("MyTicketsTable", () => {
  test("renders table-like rows with severity and priority tooltips", () => {
    renderApp(
      <MyTicketsTable
        tickets={[
          createTicket({
            assignedTo: {
              avatarUrl: null,
              firstName: "Bob",
              lastName: "Johnson",
              userId: "user-2"
            },
            board: {
              active: true,
              boardId: "board-42",
              name: "Backend board"
            },
            createdBy: {
              avatarUrl: null,
              firstName: "Alice",
              lastName: "Example",
              userId: "user-1"
            },
            name: "Fix auth refresh",
            priority: 1,
            severityName: "major",
            ticketId: "ticket-42"
          })
        ]}
      />
    );

    expect(screen.getByText("Severity")).toBeInTheDocument();
    expect(screen.getByText("Priority")).toBeInTheDocument();
    expect(screen.getByText("Ticket")).toBeInTheDocument();
    expect(screen.getByText("Board")).toBeInTheDocument();
    expect(screen.getByText("Created by")).toBeInTheDocument();
    expect(screen.getByText("Assigned to")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Fix auth refresh" })).toHaveAttribute(
      "href",
      "/tickets/ticket-42"
    );
    expect(screen.getByRole("link", { name: "Backend board" })).toHaveAttribute(
      "href",
      "/boards/board-42"
    );
    expect(screen.getByText("Alice")).toBeInTheDocument();
    expect(screen.getByText("Example")).toBeInTheDocument();
    expect(screen.getByText("Bob")).toBeInTheDocument();
    expect(screen.getByText("Johnson")).toBeInTheDocument();
    expect(screen.getByLabelText("Severity: Major")).toBeInTheDocument();
    expect(screen.getByLabelText("Priority: 1 - Immediate")).toBeInTheDocument();
  });

  test("shows empty state when there are no tickets", () => {
    renderApp(<MyTicketsTable tickets={[]} />);

    expect(screen.getByText("No tickets found.")).toBeInTheDocument();
  });
});
