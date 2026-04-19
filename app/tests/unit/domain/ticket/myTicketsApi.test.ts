import { countMyTickets, filterMyTickets, MY_TICKETS_PER_PAGE } from "../../../../src/domain/ticket/myTicketsApi";
import type { Ticket } from "../../../../src/domain/ticket/graphql";

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

describe("myTicketsApi", () => {
  test("filters by keyword, severity, and priority", () => {
    const tickets = [
      createTicket({
        description: "Auth integration",
        modifiedAt: "2026-04-03T00:00:00Z",
        name: "Alpha",
        priority: 1,
        severityId: "2",
        ticketId: "ticket-1"
      }),
      createTicket({
        description: "UI polish",
        modifiedAt: "2026-04-02T00:00:00Z",
        name: "Beta",
        priority: 5,
        severityId: "3",
        ticketId: "ticket-2"
      }),
      createTicket({
        description: "Auth tests",
        modifiedAt: "2026-04-01T00:00:00Z",
        name: "Gamma",
        priority: 1,
        severityId: "2",
        ticketId: "ticket-3"
      })
    ];

    const result = filterMyTickets(tickets, {
      keyword: "auth",
      page: 1,
      priorities: [1],
      severityIds: ["2"]
    });

    expect(result.map(ticket => ticket.ticketId)).toEqual(["ticket-1", "ticket-3"]);
    expect(
      countMyTickets(tickets, {
        keyword: "auth",
        priorities: [1],
        severityIds: ["2"]
      })
    ).toBe(2);
  });

  test("returns all filtered tickets in modifiedAt desc order", () => {
    const tickets = Array.from({ length: MY_TICKETS_PER_PAGE + 5 }, (_value, index) => {
      return createTicket({
        modifiedAt: `2026-04-${String(index + 1).padStart(2, "0")}T00:00:00Z`,
        name: `Ticket ${String(index + 1)}`,
        ticketId: `ticket-${String(index + 1)}`
      });
    });

    const result = filterMyTickets(tickets, { page: 1 });

    expect(MY_TICKETS_PER_PAGE).toBe(30);
    expect(result).toHaveLength(MY_TICKETS_PER_PAGE + 5);
    expect(result[0]?.ticketId).toBe(`ticket-${String(MY_TICKETS_PER_PAGE + 5)}`);
    expect(result.at(-1)?.ticketId).toBe("ticket-1");
  });
});
