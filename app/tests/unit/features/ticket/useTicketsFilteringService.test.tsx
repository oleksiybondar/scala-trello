import { act, renderHook } from "@testing-library/react";

import type { Ticket } from "../../../../src/domain/ticket/graphql";
import { useTicketsFilteringService } from "../../../../src/domain/ticket/useTicketsFilteringService";

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

describe("useTicketsFilteringService", () => {
  test("keeps column priority directions by status and allows updating one column independently", () => {
    const ticketsRef = {
      current: {
        byId: {},
        ids: []
      }
    };
    const { result } = renderHook(() => useTicketsFilteringService({ ticketsRef }));

    expect(result.current.columnPriorityDirections.new).toBe("high_to_low");
    expect(result.current.columnPriorityDirections.in_progress).toBe("high_to_low");

    act(() => {
      result.current.setColumnPriorityDirection("in_progress", "low_to_high");
    });

    expect(result.current.columnPriorityDirections.in_progress).toBe("low_to_high");
    expect(result.current.columnPriorityDirections.new).toBe("high_to_low");
  });

  test("applies severity and priority multi-select filters", () => {
    const ticketsRef = {
      current: {
        byId: {
          "ticket-1": createTicket({
            assignedToUserId: "user-1",
            name: "Alpha fix",
            priority: 1,
            severityId: "critical",
            severityName: "critical",
            ticketId: "ticket-1"
          }),
          "ticket-2": createTicket({
            assignedToUserId: "user-2",
            name: "Beta improvement",
            priority: 6,
            severityId: "minor",
            severityName: "minor",
            ticketId: "ticket-2"
          }),
          "ticket-3": createTicket({
            assignedToUserId: "user-1",
            name: "Gamma cleanup",
            priority: 3,
            severityId: "critical",
            severityName: "critical",
            ticketId: "ticket-3"
          })
        },
        ids: ["ticket-1", "ticket-2", "ticket-3"]
      }
    };
    const { result } = renderHook(() => useTicketsFilteringService({ ticketsRef }));

    act(() => {
      result.current.setAssignedToUserIds(["user-1"]);
      result.current.setSearchKeywords("a");
      result.current.setSelectedSeverityIds(["critical"]);
      result.current.setSelectedPriorities([1, 3]);
    });

    expect(result.current.applyFiltering().map(ticket => ticket.ticketId)).toEqual([
      "ticket-1",
      "ticket-3"
    ]);
  });
});
